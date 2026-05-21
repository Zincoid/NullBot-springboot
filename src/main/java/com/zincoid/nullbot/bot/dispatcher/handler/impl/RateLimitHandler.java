package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.CommandEvent;
import com.zincoid.nullbot.core.component.control.CommandRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class RateLimitHandler implements Handler {

    @Value("${nullbot.command.limit}")
    private boolean enabled;

    private final CommandRateLimiter commandRateLimiter;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception
    {
        if (!enabled) {
            log.info("\t\t├─[RateLimitHandler] 未启用速率限制");
            chain.doHandle(bot, event, command);
            return;
        }

        if (!event.isRateLimit()) {
            log.info("\t\t├─[RateLimitHandler] 无需速率限制");
            chain.doHandle(bot, event, command);
            return;
        }

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (
                    commandRateLimiter.tryConsume(
                            groupMessageEvent.getGroupId(),
                            groupMessageEvent.getUserId(),
                            event.getCommandType())
            ) {
                log.info("\t\t├─[RateLimitHandler] 基本消息未达到速率限制");
                chain.doHandle(bot, event, command);
            } else {
                log.info("\t\t├─[RateLimitHandler] 基本消息达到速率限制");
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "请求太多啦！", false);
            }
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            if (pokeNoticeEvent.getGroupId() == null) {
                log.info("\t\t├─[PermissionHandler] 私信戳戳事件不限速");
                chain.doHandle(bot, event, command);
                return;
            }
            if (
                    commandRateLimiter.tryConsume(
                            pokeNoticeEvent.getGroupId(),
                            pokeNoticeEvent.getUserId(),
                            event.getCommandType())
            ) {
                log.info("\t\t├─[RateLimitHandler] 戳一戳未达到速率限制");
                chain.doHandle(bot, event, command);
            } else {
                log.info("\t\t├─[RateLimitHandler] 戳一戳达到速率限制");
            }
        } else {
            log.info("\t\t├─[RateLimitHandler] 默认不限速的事件类型");
            chain.doHandle(bot, event, command);
        }
    }
}
