package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
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
public class RegisterHandler implements Handler {
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception
    {
        // 提取群ID和用户ID
        Long groupId = extractGroupId(event.getEvent());
        Long userId = extractUserId(event.getEvent());

        // 如果事件包含群ID或用户ID，则进行注册
        if (groupId != null || userId != null) {
            if(groupId != null) registerGroup(groupId);
            if(userId != null) registerUser(userId);
        } else {
            log.info("\t\t├─[RegisterHandler] 默认不注册的事件");
        }
        chain.doHandle(bot, event, command);
    }

    private void registerGroup(Long groupId) {
        GroupPO group = groupService.getGroup(groupId);
        if (group == null) {
            groupService.addGroup(groupId);
            log.info("\t\t├─[RegisterHandler] 新群聊注册完成");
        } else {
            log.info("\t\t├─[RegisterHandler] 群聊已注册");
        }
    }

    private void registerUser(Long userId) {
        UserPO user = userService.getUser(userId);
        if (user == null) {
            userService.addUser(userId);
            log.info("\t\t├─[RegisterHandler] 新用户注册完成");
        } else {
            log.info("\t\t├─[RegisterHandler] 用户已注册");
        }
    }

    private Long extractGroupId(Event event) {
        if (event instanceof GroupMessageEvent e) {
            return e.getGroupId();
        } else if (event instanceof PokeNoticeEvent e) {
            return e.getGroupId();
        } else if (event instanceof GroupMsgDeleteNoticeEvent e) {
            return e.getGroupId();
        }
        return null;
    }

    private Long extractUserId(Event event) {
        if (event instanceof GroupMessageEvent e) {
            return e.getUserId();
        } else if (event instanceof PokeNoticeEvent e) {
            return e.getUserId();
        } else if (event instanceof GroupMsgDeleteNoticeEvent e) {
            return e.getUserId();
        }
        return null;
    }
}