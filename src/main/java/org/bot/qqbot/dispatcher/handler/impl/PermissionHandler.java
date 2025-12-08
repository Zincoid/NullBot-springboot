package org.bot.qqbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.qqbot.command.Command;
import org.bot.qqbot.dispatcher.CommandHandlerChain;
import org.bot.qqbot.dispatcher.handler.Handler;
import org.bot.qqbot.entity.CommandEvent;
import org.bot.qqbot.plugin.component.control.AccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(0)
@Component
@RequiredArgsConstructor
public class PermissionHandler implements Handler
{
    private static final Logger logger = LoggerFactory.getLogger(PermissionHandler.class);
    private final AccessManager accessManager;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        if(event.isAuthRequired()){
            if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){
                int userAccess = accessManager.getAccess(groupMessageEvent.getSender().getUserId());
                int commandAccess = command.getAccess();
                if (userAccess >= commandAccess) {
                    logger.info("\t\t├─[PermissionHandler] 限权满足");
                    chain.doHandle(bot, event, command);
                }
                else {
                    logger.info("\t\t├─[PermissionHandler] 限权不足");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Permission Denied] 限权不足: 需要限权等级" + commandAccess + ", 你的限权等级为" + userAccess, false);
                }
            }else{
                logger.info("\t\t├─[PermissionHandler] 默认通过的事件类型");
                chain.doHandle(bot, event, command);
            }
        }else{
            logger.info("\t\t├─[PermissionHandler] 无需验证限权");
            chain.doHandle(bot, event, command);
        }
    }
}
