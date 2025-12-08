package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(3)
@Component
public class ExecutorHandler implements Handler
{
    private static final Logger logger = LoggerFactory.getLogger(ExecutorHandler.class);

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        logger.info("\t\t└─[ExecutorHandler] 开始执行");
        command.execute(bot, event);
        logger.info("\t\t┌─[ExecutorHandler] 结束执行");
    }
}