package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.service.StatisticService;
import com.zincoid.nullbot.core.component.tool.WsSender;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(3)
@Component
@RequiredArgsConstructor
public class StatisticHandler implements Handler {

    private final StatisticService statisticService;
    private final WsSender wsSender;

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
            log.info("├─[StatisticHandler] 默认不记录的事件");
            chain.doHandle(bot, event, command);
            return;
        }

        // WebSocketHandler.broadcast(
        //         groupId == 0 ? "私聊" : "群聊" + groupId,
        //         "%s(%s) -> %s %s".formatted(
        //                 bot.getStrangerInfo(userId, true).getData().getNickname(),
        //                 userId,
        //                 commandType,
        //                 String.join(" ", event.getCommandParameters())
        //         )
        // );

        wsSender.broadcast(
                groupId == 0 ? "私聊" : "群聊 " + groupId,
                "%s(%s) -> %s %s".formatted(
                        bot.getStrangerInfo(userId, true).getData().getNickname(),
                        userId,
                        commandType,
                        String.join(" ", event.getCommandParameters())
                )
        );

        String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
        statisticService.increase(groupId, userId, userName, commandType);
        statisticService.increaseOnDate();
        log.info("├─[StatisticHandler] 指令记录完成");

        chain.doHandle(bot, event, command);
    }
}