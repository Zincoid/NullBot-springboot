package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.control.AccessManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(0)
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionHandler implements Handler
{
    private final AccessManager accessManager;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        if(event.isAuthRequired()){
            if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){
                int userAccess = accessManager.getAccess(groupMessageEvent.getSender().getUserId());
                int commandAccess = command.getAccess();
                if (userAccess >= commandAccess) {
                    log.info("\t\t├─[PermissionHandler] 限权满足");
                    chain.doHandle(bot, event, command);
                }
                else {
                    log.info("\t\t├─[PermissionHandler] 限权不足");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Permission Denied] 限权不足: 需要限权等级" + commandAccess + ", 你的限权等级为" + userAccess, false);
                }
            }else{
                log.info("\t\t├─[PermissionHandler] 默认通过的事件类型");
                chain.doHandle(bot, event, command);
            }
        }else{
            log.info("\t\t├─[PermissionHandler] 无需验证限权");
            chain.doHandle(bot, event, command);
        }
    }
}
