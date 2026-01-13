package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotRuntimeException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(4)
@Component
@Slf4j
public class ExecutorHandler implements Handler
{
    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        log.info("\t\t└─[ExecutorHandler] 执行开始");

        try {
            command.execute(bot, event);
        }

        catch (NullBotRuntimeException e) {
            Long groupId = 0L;
            if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent)
                groupId = groupMessageEvent.getGroupId();
            else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent)
                groupId = pokeNoticeEvent.getGroupId();
            else if(event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent)
                groupId = groupMsgDeleteNoticeEvent.getGroupId();
            if (groupId != 0L) {
                bot.sendGroupMsg(groupId, e.getMessage(), false);
                log.info("\t\t  [ExecutorHandler] 指令出错: {}", e.getMessage());
            } else
                log.info("\t\t  [ExecutorHandler] 群信息获取失败");
        }

        catch (Exception e) {
            log.info("\t\t  [ExecutorHandler] Exception: ", e);
        }

        log.info("\t\t┌─[ExecutorHandler] 执行结束");
    }
}