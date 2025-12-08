package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
public class HelpHandler implements Handler
{
    private static final Logger logger = LoggerFactory.getLogger(HelpHandler.class);

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception
    {
        if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){
            if (event.getCommandParameters()!=null && !event.getCommandParameters().isEmpty()) {
                if ("-help".equalsIgnoreCase(event.getCommandParameters().get(0)) || "-h".equalsIgnoreCase(event.getCommandParameters().get(0))) {
                    logger.info("\t\t├─[HelpHandler] 已输出 详细帮助");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), command.getHelp(), false);
                    return;
                }
            }
            logger.info("\t\t├─[HelpHandler] 非帮助命令");
            chain.doHandle(bot, event, command);
        }else{
            logger.info("\t\t├─[HelpHandler] 默认无帮助的事件");
            chain.doHandle(bot, event, command);
        }
    }
}
