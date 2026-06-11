package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.gateway.processor.CommandHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CommandEvent;
import com.zincoid.nullbot.core.module.control.CommandRateLimiter;
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
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        if (!enabled) {
            log.info("├─[RateLimitHandler] 未启用限速器");
            chain.doHandle(bot, event, command);
            return;
        }
        if (!event.isRateLimit()) {
            log.info("├─[RateLimitHandler] 事件无需限速");
            chain.doHandle(bot, event, command);
            return;
        }
        if (event.getEventScope() == EventScope.PRIVATE) {
            log.info("├─[RateLimitHandler] 私聊无需限速");
            chain.doHandle(bot, event, command);
            return;
        }

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        if (commandRateLimiter.tryConsume(groupId, userId, event.getCommandType())) {
            log.info("├─[RateLimitHandler] 未达速率限制");
            chain.doHandle(bot, event, command);
        } else {
            log.info("├─[RateLimitHandler] 达到速率限制");
            // 仅文字消息提示限速 戳一戳等交互静默忽略
            if (event.getEvent() instanceof GroupMessageEvent)
                bot.sendGroupMsg(groupId, "请求太多啦！", false);
        }
    }
}
