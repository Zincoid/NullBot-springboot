package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(-1)
@Component
@RequiredArgsConstructor
@Slf4j
public class RegisterHandler implements Handler
{
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        Long groupId = 0L;
        Long userId = 0L;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
            userId = groupMessageEvent.getUserId();
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            groupId = pokeNoticeEvent.getGroupId();
            userId = pokeNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
            groupId = groupMsgDeleteNoticeEvent.getGroupId();
            userId = groupMsgDeleteNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
            userId = privateMessageEvent.getUserId();
        } else {
            log.info("\t\t├─[RegisterHandler] 默认不注册的事件");
            chain.doHandle(bot, event, command);
            return;
        }

        if (groupId != 0L) {
            GroupInfoResp groupData = bot.getGroupInfo(groupId, true).getData();
            if (groupData != null) registerGroup(groupId, groupData.getGroupName());
        }
        if (userId != 0L) {
            StrangerInfoResp userData = bot.getStrangerInfo(userId, true).getData();
            if (userData != null) registerUser(userId, userData.getNickname());
        }

        chain.doHandle(bot, event, command);
    }

    private void registerGroup(Long groupId, String groupName) {
        GroupPO group = groupService.getGroup(groupId);
        if (group == null) {
            groupService.addGroup(groupId, groupName);
            log.info("\t\t├─[RegisterHandler] 新群聊注册完成");
        } else {
            groupService.updateGroupName(groupId, groupName);
            log.info("\t\t├─[RegisterHandler] 群聊已注册 -> 更新完成");
        }
    }

    private void registerUser(Long userId, String userName) {
        UserPO user = userService.getUser(userId);
        if (user == null) {
            userService.addUser(userId, userName);
            log.info("\t\t├─[RegisterHandler] 新用户注册完成");
        } else {
            userService.updateUserName(userId, userName);
            log.info("\t\t├─[RegisterHandler] 用户已注册 -> 更新完成");
        }
    }
}
