package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.StatisticService;
import org.bot.nullbot.websocket.WebSocketLogger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Order(3)
@Component
@Slf4j
@RequiredArgsConstructor
public class StatisticHandler implements Handler
{
    private final StatisticService statisticService;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        String commandType = command.getClass().getSimpleName().replace("Command", "");
        Long groupId;
        Long userId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupId = groupMessageEvent.getGroupId();
            userId = groupMessageEvent.getUserId();
        } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
            groupId = pokeNoticeEvent.getGroupId() == null ? 0L : pokeNoticeEvent.getGroupId();  // 群号 0 代表私聊
            userId = pokeNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
            groupId = groupMsgDeleteNoticeEvent.getGroupId();
            userId = groupMsgDeleteNoticeEvent.getUserId();
        } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
            groupId = 0L;  // 群号 0 代表私聊
            userId = privateMessageEvent.getUserId();
        } else {
            log.info("\t\t├─[StatisticHandler] 默认不记录的事件");
            chain.doHandle(bot, event, command);
            return;
        }

        WebSocketLogger.broadcast(
                "[NullBot-%s-%s] 用户%s 调用 %s 指令"
                .formatted(
                        LocalDateTime.now(),
                        groupId == 0 ? "私聊" : "群聊" + groupId,
                        userId,
                        commandType
                )
        );

        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        statisticService.increase(groupId, userId, userName, commandType);
        statisticService.increaseOnDate();
        log.info("\t\t├─[StatisticHandler] 指令记录完成");

        chain.doHandle(bot, event, command);
    }
}