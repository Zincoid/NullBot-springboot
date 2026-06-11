package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.gateway.processor.CommandHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CommandEvent;
import com.zincoid.nullbot.core.service.system.StatsService;
import com.zincoid.nullbot.core.module.system.WsSender;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class StatsHandler implements Handler {

    private final StatsService statsService;
    private final WsSender wsSender;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        String commandType = command.getClass().getSimpleName().replace("Command", "");
        EventScope eventScope = event.getEventScope();

        if (eventScope == EventScope.UNKNOWN) {
            log.info("├─[StatsHandler] 默认不记录的事件");
            chain.doHandle(bot, event, command);
            return;
        }

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        wsSender.broadcast(
                eventScope == EventScope.PRIVATE ? "私聊" : "群聊 " + groupId,
                "%s(%s) -> %s %s".formatted(
                        bot.getStrangerInfo(userId, true).getData().getNickname(),
                        userId,
                        commandType,
                        String.join(" ", event.getCommandParameters())
                )
        );

        statsService.increase(groupId, userId, commandType);
        statsService.increaseDaily();
        log.info("├─[StatsHandler] 指令记录完成");

        chain.doHandle(bot, event, command);
    }
}