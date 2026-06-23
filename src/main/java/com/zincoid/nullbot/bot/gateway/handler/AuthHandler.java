package com.zincoid.nullbot.bot.gateway.handler;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.command.Cmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.core.service.base.GroupService;
import com.zincoid.nullbot.core.service.base.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Order(0)
@Component
@RequiredArgsConstructor
public class AuthHandler implements Handler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
    private static final String ACCESS_DENIED_MSG = """
            🚫访问限权不足
            - 需要限权: %s
            - 你的限权: %s""";

    private final Map<Long, List<String>> bannedCmds = new ConcurrentHashMap<>();  // GroupId -> CmdNames
    private final Map<String, LocalDateTime> bannedUsers = new ConcurrentHashMap<>();  // UserId#CmdName -> BanUntil
    private final Set<Long> allowedPrivateUsers = new ConcurrentHashSet<>();  // UserId

    private final GroupService groupService;
    private final UserService userService;

    private final AtomicBoolean inMaintenance = new AtomicBoolean(false);

    @Override
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {

        Class<? extends Cmd> cmdClass = cmd.getClass();
        List<String> params = event.getCmdParams();
        EventScope eventScope = event.getEventScope();

        // =================== 未知类型验证 ===================

        if (eventScope == EventScope.UNKNOWN) {
            log.info("├─[AuthHandler] 未知事件默认通过");
            chain.doHandle(bot, event, cmd);
            return;
        }

        // =================== 私聊类型验证 ===================

        if (eventScope == EventScope.PRIVATE) {
            Long userId = event.getUserId();
            if (inMaintenance.get()) {
                log.info("├─[AuthHandler] 系统已锁定");
                bot.sendPrivateMsg(userId, "🔐系统已锁定", false);
                return;
            }
            if (allowedPrivateUsers.contains(userId)) {
                log.info("├─[AuthHandler] 私聊用户已授权");
                chain.doHandle(bot, event, cmd);
                return;
            }
            log.info("├─[AuthHandler] 私聊用户未授权");
            bot.sendPrivateMsg(userId, """
                    🚫私聊未授权
                    - 输入"#密钥\"""", false);
            return;
        }

        // =================== 群聊类型验证 ===================

        Long userId = event.getUserId();
        Long groupId = event.getGroupId();

        // ------------------- 限权信息查询 -------------------

        int cmdAccess = cmd.getAccess();
        int groupAccess = groupService.getAccess(groupId);
        int userAccess = userService.getAccess(userId);

        // ------------------- 系统锁定验证 -------------------

        if (inMaintenance.get() && userAccess < 2) {
            log.info("├─[AuthHandler] 系统已锁定");
            bot.sendGroupMsg(groupId, """
                    🔒系统已锁定
                    - 操作需限权II""", false);
            return;
        }

        // ------------------- 指令限权验证 -------------------

        if (groupAccess >= cmdAccess) {
            log.info("├─[AuthHandler] 群限权满足");
        } else {
            log.info("├─[AuthHandler] 群限权不足");
            return;
        }

        if (event.isAuthRequired()) {
            if (userAccess >= cmdAccess) {
                log.info("├─[AuthHandler] 用户限权满足");
            } else {
                log.info("├─[AuthHandler] 用户限权不足");
                bot.sendGroupMsg(groupId, ACCESS_DENIED_MSG.formatted(cmdAccess, userAccess), false);
                return;
            }
        } else {
            log.info("├─[AuthHandler] 无需验证用户限权");
        }

        // ------------------- 群组停用验证 -------------------

        if (!params.isEmpty() && ("--toggle".equals(params.getFirst()) || "-T".equals(params.getFirst()))) {
            if (userAccess < 1) {
                log.info("├─[AuthHandler] 停用限权不足");
                bot.sendGroupMsg(groupId, ACCESS_DENIED_MSG.formatted(1, userAccess), false);
                return;
            }
            boolean banned = switchCmdBan(groupId, cmdClass);
            log.info("├─[AuthHandler] 群组指令 {}-{} {}",
                    groupId, cmdClass.getSimpleName(), banned ? "已停用" : "已启用");
            bot.sendGroupMsg(groupId, banned ? "⛔️已停用" : "✅已启用", false);
            return;
        }

        if (isCmdBanned(groupId, cmdClass)) {
            log.info("├─[AuthHandler] 群组指令 {}-{} 停用中",
                    groupId, cmdClass.getSimpleName());
            bot.sendGroupMsg(groupId, "⛔️停用中", false);
            return;
        }

        // ------------------- 用户禁用验证 -------------------

        if (!params.isEmpty() && ("--ban".equals(params.getFirst()) || "-B".equals(params.getFirst()))) {
            if (userAccess < 1) {
                log.info("├─[AuthHandler] 封禁限权不足");
                bot.sendGroupMsg(groupId, ACCESS_DENIED_MSG.formatted(1, userAccess), false);
                return;
            }
            if (params.size() < 3) {
                log.info("├─[AuthHandler] 封禁参数不足");
                bot.sendGroupMsg(groupId, "❌参数不足", false);
                return;
            }
            long targetId;
            int banTime;
            try {
                targetId = Long.parseLong(params.get(1));
                banTime = Integer.parseInt(params.get(2));
            } catch (NumberFormatException e) {
                log.info("├─[AuthHandler] 封禁参数非法");
                bot.sendGroupMsg(groupId, "❌参数非法", false);
                return;
            }
            if (!userService.exist(targetId)) {
                log.info("├─[AuthHandler] 封禁用户未注册");
                bot.sendGroupMsg(groupId, "❌无此用户", false);
                return;
            }
            setUserBan(targetId, cmdClass, banTime);
            log.info("├─[AuthHandler] 用户指令 {}-{} {}",
                    targetId, cmdClass.getSimpleName(), banTime > 0 ? "已封禁 " + banTime + " Min" : "已解封");
            bot.sendGroupMsg(groupId, banTime > 0 ? "⛔️已封禁" : "✅已解封", false);
            return;
        }

        if (isUserBanned(userId, cmdClass)) {
            LocalDateTime until = getUserBannedUntil(userId, cmdClass);
            Duration duration = Duration.between(LocalDateTime.now(), until).abs();
            long totalSeconds = duration.getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            log.info("├─[AuthHandler] 用户指令 {}-{} 被封禁至 {}",
                    userId, cmdClass.getSimpleName(), until.format(FORMATTER));
            bot.sendGroupMsg(groupId, """
                    ⛔️你已被禁用该指令
                    - 将于 %sh %sm %ss 后解封""".formatted(hours, minutes, seconds), false);
            return;
        }

        chain.doHandle(bot, event, cmd);
    }

    // =================== 系统锁定方法 ===================

    public boolean switchInMaintenance() {
        boolean current, next;
        do {
            current = inMaintenance.get();
            next = !current;
        } while (!inMaintenance.compareAndSet(current, next));
        return next;
    }

    // =================== 私聊授权方法 ===================

    public void addAllowedPrivateUser(Long userId) {
        allowedPrivateUsers.add(userId);
    }

    public void removeAllowedPrivateUser(Long userId) {
        allowedPrivateUsers.remove(userId);
    }

    // =================== 群聊封禁方法 ===================

    public boolean switchCmdBan(Long groupId, Class<? extends Cmd> cmdClass) {
        String cmdName = cmdClass.getSimpleName();
        List<String> banList = bannedCmds.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>());
        if (banList.contains(cmdName)) {
            banList.remove(cmdName);
            return false;
        } else {
            banList.add(cmdName);
            return true;
        }
    }

    public boolean isCmdBanned(Long groupId, Class<? extends Cmd> cmdClass) {
        List<String> banList = bannedCmds.get(groupId);
        return banList != null && banList.contains(cmdClass.getSimpleName());
    }

    public void setUserBan(Long userId, Class<? extends Cmd> cmdClass, int time) {
        String key = "%s-%s".formatted(userId, cmdClass.getSimpleName());
        bannedUsers.put(key, LocalDateTime.now().plusMinutes(time));
    }

    public boolean isUserBanned(Long userId, Class<? extends Cmd> cmdClass) {
        String key = "%s-%s".formatted(userId, cmdClass.getSimpleName());
        LocalDateTime banUntil = bannedUsers.get(key);
        if (banUntil == null) return false;
        if (LocalDateTime.now().isAfter(banUntil)) {
            bannedUsers.remove(key);
            return false;
        }
        return true;
    }

    public LocalDateTime getUserBannedUntil(Long userId, Class<? extends Cmd> cmdClass) {
        String key = "%s-%s".formatted(userId, cmdClass.getSimpleName());
        return bannedUsers.get(key);
    }
}
