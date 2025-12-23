package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.StatisticService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

        if(event.getEvent() instanceof GroupMessageEvent groupMessageEvent){
            statisticService.increaseOnDate();
            statisticService.increase(groupMessageEvent.getGroupId(), groupMessageEvent.getSender().getUserId(), groupMessageEvent.getSender().getNickname(), commandType);
            log.info("\t\t├─[StatisticHandler] 基本指令记录完成");
            chain.doHandle(bot, event, command);
        }else if(event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent){
            Long userId = pokeNoticeEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            statisticService.increaseOnDate();
            statisticService.increase(pokeNoticeEvent.getGroupId(), userId, userName, commandType);
            log.info("\t\t├─[StatisticHandler] 戳一戳指令记录完成");
            chain.doHandle(bot, event, command);
        }else if(event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent){
            Long userId = groupMsgDeleteNoticeEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            statisticService.increaseOnDate();
            statisticService.increase(groupMsgDeleteNoticeEvent.getGroupId(), userId, userName, commandType);
            log.info("\t\t├─[StatisticHandler] 撤回反应指令记录完成");
            chain.doHandle(bot, event, command);
        }else{
            log.info("\t\t├─[StatisticHandler] 默认不记录的事件");
            chain.doHandle(bot, event, command);
        }
    }
}