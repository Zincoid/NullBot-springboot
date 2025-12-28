package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
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
        if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){

            GroupPO group = groupService.getGroup(groupMessageEvent.getGroupId());
            if(group == null){
                groupService.addGroup(groupMessageEvent.getGroupId());
                log.info("\t\t├─[RegisterHandler] 新群聊注册完成");
            }else{
                log.info("\t\t├─[RegisterHandler] 群聊已注册");
            }

            UserPO user = userService.getUser(groupMessageEvent.getUserId());
            if(user == null){
                userService.addUser(groupMessageEvent.getUserId());
                log.info("\t\t├─[RegisterHandler] 新用户注册完成");
            }else{
                log.info("\t\t├─[RegisterHandler] 用户已注册");
            }

            chain.doHandle(bot, event, command);
        }else{
            log.info("\t\t├─[RegisterHandler] 默认不注册的事件");
            chain.doHandle(bot, event, command);
        }
    }
}
