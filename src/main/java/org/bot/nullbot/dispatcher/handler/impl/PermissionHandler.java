package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Order(0)
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHandler implements Handler
{
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    private final Map<Long, List<String>> bannedCmds = new ConcurrentHashMap<>();  // GroupId -> CommandClasses
    private final Map<String, LocalDateTime> bannedUsers = new ConcurrentHashMap<>();  // UserId + CommandClass -> BanUntil
    private boolean inMaintenance = false;

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {

        Class<? extends Command> commandClass = command.getClass();
        List<String> params = event.getCommandParameters();
        Long groupId;
        Long userId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
            userId = groupMessageEvent.getUserId();
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            groupId = pokeNoticeEvent.getGroupId() == null ? 0L : pokeNoticeEvent.getGroupId();
            userId = pokeNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent  groupMsgDeleteNoticeEvent) {
            groupId = groupMsgDeleteNoticeEvent.getGroupId();
            userId = groupMsgDeleteNoticeEvent.getUserId();
        } else {
            log.info("\t\t├─[PermissionHandler] 默认通过的事件类型");
            chain.doHandle(bot, event, command);
            return;
        }

        if (groupId == 0L) {  // 群号 0 代表私聊
            log.info("\t\t├─[PermissionHandler] 私信事件放行");
            chain.doHandle(bot, event, command);
            return;
        }

        int commandAccess = command.getAccess();
        int groupAccess = groupService.getGroupAccess(groupId);
        int userAccess = userService.getUserAccess(userId);

        // =================== 系统锁定验证 ===================

        if (inMaintenance && userAccess < 2) {
            log.info("\t\t├─[PermissionHandler] 系统已锁定");
            bot.sendGroupMsg(groupId, """
                        [访问] 🔐系统已锁定
                        - 操作需限权等级II""", false);
            return;
        }

        // =================== 指令限权验证 ===================

        if (groupAccess >= commandAccess) {
            log.info("\t\t├─[PermissionHandler] 群限权满足");
        } else {
            log.info("\t\t├─[PermissionHandler] 群限权不足");
            return;
        }

        if (event.isAuthRequired()) {
            if (userAccess >= commandAccess) {
                log.info("\t\t├─[PermissionHandler] 用户限权满足");
            } else {
                log.info("\t\t├─[PermissionHandler] 用户限权不足");
                bot.sendGroupMsg(groupId, """
                        [访问] 🚫限权不足
                        - 需要限权等级: %s
                        - 你的限权等级: %s""".formatted(commandAccess, userAccess), false);
                return;
            }
        } else {
            log.info("\t\t├─[PermissionHandler] 无需验证用户限权");
        }

        // =================== 群组停用验证 ===================

        if (!params.isEmpty() && "-x".equals(params.getFirst())) {
            if (userAccess < 1) {
                log.info("\t\t├─[PermissionHandler] 停用限权不足");
                bot.sendGroupMsg(groupId, """
                    [访问] 🚫限权不足
                    - 需要限权等级: 1
                    - 你的限权等级: %s""".formatted(userAccess), false);
                return;
            }
            boolean banned = switchCmdBan(groupId, commandClass);
            log.info("\t\t├─[PermissionHandler] 群组指令 {}-{} {}",
                    groupId, commandClass.getSimpleName(), banned ? "已停用" : "已启用");
            bot.sendGroupMsg(groupId, "[访问] %s".formatted(banned ? "⛔️已停用" : "✅已启用"), false);
            return;
        }

        if (isCmdBanned(groupId, commandClass)) {
            log.info("\t\t├─[PermissionHandler] 群组指令 {}-{} 停用中",
                    groupId, commandClass.getSimpleName());
            bot.sendGroupMsg(groupId, "[访问] ⛔️停用中", false);
            return;
        }

        // =================== 用户禁用验证 ===================

        if (!params.isEmpty() && "-b".equals(params.getFirst())) {
            if (userAccess < 1) {
                log.info("\t\t├─[PermissionHandler] 封禁限权不足");
                bot.sendGroupMsg(groupId, """
                        [访问] 🚫限权不足
                        - 需要限权等级: 1
                        - 你的限权等级: %s""".formatted(userAccess), false);
                return;
            }
            if (params.size() < 3) {
                log.info("\t\t├─[PermissionHandler] 封禁参数不足");
                bot.sendGroupMsg(groupId, "[访问] ❌封禁参数不足", false);
                return;
            }
            long targetId;
            int banTime;
            try {
                targetId = Long.parseLong(params.get(1));
                banTime = Integer.parseInt(params.get(2));
            } catch (NumberFormatException e) {
                log.info("\t\t├─[PermissionHandler] 封禁参数非法");
                bot.sendGroupMsg(groupId, "[访问] ❌封禁参数非法", false);
                return;
            }
            if (!userService.existUser(targetId)) {
                log.info("\t\t├─[PermissionHandler] 封禁用户未注册");
                bot.sendGroupMsg(groupId, "[访问] ❌封禁用户未注册", false);
                return;
            }
            setUserBan(targetId, commandClass, banTime);
            log.info("\t\t├─[PermissionHandler] 用户指令 {}-{} {}",
                    targetId, commandClass.getSimpleName(), banTime > 0 ? "已封禁 " + banTime + " Min" : "已解封");
            bot.sendGroupMsg(groupId, "[访问] %s".formatted(banTime > 0 ? "⛔️已封禁" : "✅已解封"), false);
            return;
        }

        if (isUserBanned(userId, commandClass)) {
            LocalDateTime until = getUserBannedUntil(userId, commandClass);
            Duration duration = Duration.between(LocalDateTime.now(), until).abs();
            long totalSeconds = duration.getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            log.info("\t\t├─[PermissionHandler] 用户指令 {}-{} 被封禁至 {}",
                    userId, commandClass.getSimpleName(), until.format(formatter));
            bot.sendGroupMsg(groupId, """
                    [访问] ⛔️你已被禁用该指令
                    - 将于 %sh %sm %ss 后解封""".formatted(hours, minutes, seconds), false);
            return;
        }

        chain.doHandle(bot, event, command);
    }

    // =================== 锁定方法 ===================

    public boolean switchInMaintenance() {
        return inMaintenance = !inMaintenance;
    }

    // =================== 封禁方法 ===================

    public boolean switchCmdBan(Long groupId, Class<? extends Command> commandClass) {
        String cmdName = commandClass.getSimpleName();
        List<String> banList = bannedCmds.computeIfAbsent(groupId, k -> new ArrayList<>());
        if (banList.contains(cmdName)) {
            banList.remove(cmdName);
            return false;
        } else {
            banList.add(cmdName);
            return true;
        }
    }

    public boolean isCmdBanned(Long groupId, Class<? extends Command> commandClass) {
        return bannedCmds.computeIfAbsent(groupId, k -> new ArrayList<>())
                .contains(commandClass.getSimpleName());
    }

    public void setUserBan(Long userId, Class<? extends Command> commandClass, int time) {
        String id = "%s-%s".formatted(userId, commandClass.getSimpleName());
        bannedUsers.put(id, LocalDateTime.now().plusMinutes(time));
    }

    public boolean isUserBanned(Long userId, Class<? extends Command> commandClass) {
        String id = "%s-%s".formatted(userId, commandClass.getSimpleName());
        LocalDateTime banUntil = bannedUsers.get(id);
        if (banUntil == null) return false; // 用户未被封禁
        if (LocalDateTime.now().isAfter(banUntil)) {
            bannedUsers.remove(id);  // 封禁时间已过 自动清理
            return false;
        }
        return true;
    }

    public LocalDateTime getUserBannedUntil(Long userId, Class<? extends Command> commandClass) {
        String id = "%s-%s".formatted(userId, commandClass.getSimpleName());
        return bannedUsers.get(id);
    }
}
