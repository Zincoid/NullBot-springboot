package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.control.CommandRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(2)
@Component
@RequiredArgsConstructor
public class RateLimitHandler implements Handler
{
    private static final Logger logger = LoggerFactory.getLogger(RateLimitHandler.class);
    private final CommandRateLimiter commandRateLimiter;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){
            if (commandRateLimiter.tryConsume(event)) {
                logger.info("\t\t├─[RateLimitHandler] 未达到速率限制");
                chain.doHandle(bot, event, command);
            }else{
                logger.info("\t\t├─[RateLimitHandler] 达到速率限制");
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "请求过多，请稍后再试", false);
            }
        }else{
            logger.info("\t\t├─[RateLimitHandler] 默认不限速的事件");
            chain.doHandle(bot, event, command);
        }
    }
}
