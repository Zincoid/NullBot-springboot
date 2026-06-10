package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.enums.EventScope;
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
        EventScope eventScope = event.getEventScope();

        if (eventScope == EventScope.UNKNOWN) {
            log.info("├─[RegisterHandler] 未知事件不注册");
            chain.doHandle(bot, event, command);
            return;
        }

        if (eventScope == EventScope.GROUP) {
            Long groupId = event.getGroupId();
            GroupInfoResp group = bot.getGroupInfo(groupId, true).getData();
            if (group != null) registerGroup(groupId, group.getGroupName());
        }

        Long userId = event.getUserId();
        StrangerInfoResp user = bot.getStrangerInfo(userId, true).getData();
        if (user != null) registerUser(userId, user.getNickname());

        chain.doHandle(bot, event, command);
    }

    private void registerGroup(Long groupId, String groupName) {
        GroupPO group = groupService.getById(groupId);
        if (group == null) {
            group = new GroupPO(groupId, groupName);
        } else group.setName(groupName);
        groupService.saveOrUpdate(group);
        log.info("├─[RegisterHandler] 群聊已更新");
    }

    private void registerUser(Long userId, String userName) {
        UserPO user = userService.getById(userId);
        if (user == null) {
            user = new UserPO(userId, userName);
        } else user.setName(userName);
        userService.saveOrUpdate(user);
        log.info("├─[RegisterHandler] 用户已更新");
    }
}
