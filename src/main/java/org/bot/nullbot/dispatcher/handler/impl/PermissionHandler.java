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
    private final Map<Long, List<String>> banMap = new ConcurrentHashMap<>();  // GroupId -> Commands
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
            userId = groupMessageEvent.getSender().getUserId();
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
            onBanningRefresh(bot, userAccess, groupId, commandClass);
            return;
        }

        if (banMap.computeIfAbsent(groupId, k -> new ArrayList<>()).contains(commandClass)) {
            log.info("\t\t├─[PermissionHandler] 群组 {} - {} 停用中", groupId, commandClass);
            bot.sendGroupMsg(groupId, "[访问] ⛔️停用中", false);
            return;
        }

        chain.doHandle(bot, event, command);
    }

    // =================== 工具方法 ===================

    private void onBanningRefresh(Bot bot, int userAccess, Long groupId, String commandClass) {
        if (userAccess < 1) {
            log.info("\t\t├─[PermissionHandler] 修改限权不足");
            bot.sendGroupMsg(groupId, """
                    [访问] 🚫限权不足
                    - 需要限权等级: 1
                    - 你的限权等级: %s""".formatted(userAccess), false);
            return;
        }
        List<String> banned = banMap.computeIfAbsent(groupId, k -> new ArrayList<>());
        if (banned.contains(commandClass)) {
            banned.remove(commandClass);
            log.info("\t\t├─[PermissionHandler] 群组 {} - {} 已启用", groupId, commandClass);
            bot.sendGroupMsg(groupId, "[访问] ✅已启用", false);
        } else {
            banned.add(commandClass);
            log.info("\t\t├─[PermissionHandler] 群组 {} - {} 已停用", groupId, commandClass);
            bot.sendGroupMsg(groupId, "[访问] ⛔️已停用", false);
        }
    }

    // =================== 锁定方法 ===================

    public boolean switchInMaintenance() {
        return inMaintenance = !inMaintenance;
    }
}
