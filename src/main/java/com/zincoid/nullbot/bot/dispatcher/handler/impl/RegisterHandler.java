package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.service.basic.GroupService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class RegisterHandler implements Handler {

    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        Long groupId;
        Long userId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
            userId = groupMessageEvent.getUserId();
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            groupId = pokeNoticeEvent.getGroupId() == null ? 0L : pokeNoticeEvent.getGroupId();  // 群号 0 代表私聊
            userId = pokeNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
            groupId = groupMsgDeleteNoticeEvent.getGroupId();
            userId = groupMsgDeleteNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
            groupId = 0L;  // 群号 0 代表私聊
            userId = privateMessageEvent.getUserId();
        } else {
            log.info("├─[RegisterHandler] 默认不注册的事件");
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
        GroupPO group = groupService.getById(groupId);
        if (group == null) {
            group = new GroupPO(groupId, groupName);
        } else group.setName(groupName);
        groupService.saveOrUpdate(group);
        log.info("├─[RegisterHandler] 群聊信息已更新");
    }

    private void registerUser(Long userId, String userName) {
        UserPO user = userService.getById(userId);
        if (user == null) {
            user = new UserPO(userId, userName);
        } else user.setName(userName);
        userService.saveOrUpdate(user);
        log.info("├─[RegisterHandler] 用户信息已更新");
    }
}
