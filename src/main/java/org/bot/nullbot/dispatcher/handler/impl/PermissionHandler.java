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
        String commandClass = command.getClass().getSimpleName();
        List<String> params = event.getCommandParameters();
        Long groupId;
        Long userId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
            userId = groupMessageEvent.getUserId();
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            groupId = pokeNoticeEvent.getGroupId();
            userId = pokeNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent  groupMsgDeleteNoticeEvent) {
            groupId = groupMsgDeleteNoticeEvent.getGroupId();
            userId = groupMsgDeleteNoticeEvent.getUserId();
        } else {
            log.info("\t\t├─[PermissionHandler] 默认通过的事件类型");
            chain.doHandle(bot, event, command);
            return;
        }

        int commandAccess = command.getAccess();
        int groupAccess = groupService.getGroupAccess(groupId);
        int userAccess = userService.getUserAccess(userId);

        if (inMaintenance && userAccess < 2) {
            log.info("\t\t├─[PermissionHandler] 系统已锁定");
            bot.sendGroupMsg(groupId, """
                        [访问] 🔐系统已锁定
                        - 操作需限权等级II""", false);
            return;
        }

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

        if (!params.isEmpty() && "-x".equals(params.getFirst())) {
            if (userAccess < 1) {
                log.info("\t\t├─[PermissionHandler] 修改限权不足");
                bot.sendGroupMsg(groupId, """
                    [访问] 🚫限权不足
                    - 需要限权等级: 1
                    - 你的限权等级: %s""".formatted(userAccess), false);
                return;
            }
            boolean banned = switchCmdBan(groupId, commandClass);
            log.info("\t\t├─[PermissionHandler] 群组 {} - {} {}", groupId, commandClass, banned ? "已停用" : "已启用");
            bot.sendGroupMsg(groupId, "[访问] %s".formatted(banned ? "⛔️已停用" : "✅已启用"), false);
            return;
        }

        if (isCmdBanned(groupId, commandClass)) {
            log.info("\t\t├─[PermissionHandler] 群组 {} - {} 停用中", groupId, commandClass);
            bot.sendGroupMsg(groupId, "[访问] ⛔️停用中", false);
            return;
        }

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
                log.info("\t\t├─[PermissionHandler] 群组 {} - 封禁参数不足", groupId);
                bot.sendGroupMsg(groupId, "[访问] ❌封禁参数不足", false);
                return;
            }
            long targetId;
            int banTime;
            try {
                targetId = Long.parseLong(params.get(1));
                banTime = Integer.parseInt(params.get(2));
            } catch (NumberFormatException e) {
                log.info("\t\t├─[PermissionHandler] 群组 {} - 封禁参数非法", groupId);
                bot.sendGroupMsg(groupId, "[访问] ❌封禁参数非法", false);
                return;
            }
            setUserBan(targetId, commandClass, banTime);
            log.info("\t\t├─[PermissionHandler] 用户 {} - {} 已封禁 {} Min", targetId, commandClass, banTime);
            bot.sendGroupMsg(groupId, "[访问] ⛔️已封禁用户指令", false);
            return;
        }

        if (isUserBanned(userId, commandClass)) {
            String until = getUserBannedUntil(userId, commandClass).format(formatter);
            log.info("\t\t├─[PermissionHandler] 用户 {} - {} 停用中", userId, commandClass);
            bot.sendGroupMsg(groupId, """
                    [访问] ⛔️你已被禁用该指令！
                    - 解封于 %s""".formatted(until), false);
            return;
        }

        chain.doHandle(bot, event, command);
    }

    // =================== 封禁方法 ===================

    private boolean switchCmdBan(Long groupId, String commandClass) {
        List<String> banList = bannedCmds.computeIfAbsent(groupId, k -> new ArrayList<>());
        if (banList.contains(commandClass)) {
            banList.remove(commandClass);
            return false;
        } else {
            banList.add(commandClass);
            return true;
        }
    }

    private boolean isCmdBanned(Long groupId, String commandClass) {
        return bannedCmds.computeIfAbsent(groupId, k -> new ArrayList<>()).contains(commandClass);
    }

    private void setUserBan(Long userId, String commandClass, int time) {
        String id = "%s-%s".formatted(userId, commandClass);
        bannedUsers.put(id, LocalDateTime.now().plusMinutes(time));
    }

    private boolean isUserBanned(Long userId, String commandClass) {
        String id = "%s-%s".formatted(userId, commandClass);
        LocalDateTime banUntil = bannedUsers.get(id);
        if (banUntil == null) return false; // 用户未被封禁
        if (LocalDateTime.now().isAfter(banUntil)) {
            bannedUsers.remove(id);  // 封禁时间已过 自动清理
            return false;
        }
        return true;
    }

    public LocalDateTime getUserBannedUntil(Long userId, String commandClass) {
        String id = "%s-%s".formatted(userId, commandClass);
        return bannedUsers.get(id);
    }

    // =================== 锁定方法 ===================

    public boolean switchInMaintenance() {
        return inMaintenance = !inMaintenance;
    }
}
