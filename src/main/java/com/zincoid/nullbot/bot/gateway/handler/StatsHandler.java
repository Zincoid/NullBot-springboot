package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
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
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {
        String command = cmd.getClass().getSimpleName().replace("Cmd", "");
        EventScope scope = event.getEventScope();

        if (scope == EventScope.UNKNOWN) {
            log.info("├─[StatsHandler] 未知事件不可记录");
            chain.doHandle(bot, event, cmd);
            return;
        }

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        wsSender.broadcast(
                scope == EventScope.PRIVATE ? "私聊" : "群聊 " + groupId,
                "%s(%s) -> %s %s".formatted(
                        bot.getStrangerInfo(userId, true).getData().getNickname(),
                        userId,
                        command,
                        String.join(" ", event.getCmdParams())
                )
        );

        statsService.increase(groupId, userId, command);
        statsService.increaseDaily();
        log.info("├─[StatsHandler] 指令记录完成");

        chain.doHandle(bot, event, cmd);
    }
}