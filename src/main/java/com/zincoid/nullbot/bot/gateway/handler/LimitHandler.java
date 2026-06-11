package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.core.module.control.CmdRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class LimitHandler implements Handler {

    @Value("${nullbot.command.limit}")
    private boolean enabled;

    private final CmdRateLimiter cmdRateLimiter;

    @Override
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {
        if (!enabled) {
            log.info("├─[LimitHandler] 未启用限速器");
            chain.doHandle(bot, event, cmd);
            return;
        }

        EventScope scope = event.getEventScope();

        if (!event.isRateLimit() || scope == EventScope.UNKNOWN) {
            log.info("├─[LimitHandler] 事件无需限速");
            chain.doHandle(bot, event, cmd);
            return;
        }
        if (scope == EventScope.PRIVATE) {
            log.info("├─[LimitHandler] 私聊无需限速");
            chain.doHandle(bot, event, cmd);
            return;
        }

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        if (cmdRateLimiter.tryConsume(groupId, userId, event.getCmdType())) {
            log.info("├─[LimitHandler] 未达速率限制");
            chain.doHandle(bot, event, cmd);
        } else {
            log.info("├─[LimitHandler] 达到速率限制");
            // 仅文字消息提示限速 戳一戳等交互静默忽略
            if (event.getEvent() instanceof GroupMessageEvent)
                bot.sendGroupMsg(groupId, "请求太多啦！", false);
        }
    }
}
