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
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
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

        Long groupId = 0L;
        try {
            if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
                groupId = groupMessageEvent.getGroupId();
                command.execute(bot, groupMessageEvent, event.getCommandParameters());
            } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
                groupId = pokeNoticeEvent.getGroupId();
                command.execute(bot, pokeNoticeEvent, event.getCommandParameters());
            } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
                groupId = groupMsgDeleteNoticeEvent.getGroupId();
                command.execute(bot, groupMsgDeleteNoticeEvent, event.getCommandParameters());
            } else
                log.warn("\t\t  [ExecutorHandler] 不可执行的事件类型");

        } catch (NullBotMsgException e) {
            if (groupId != 0L) {
                bot.sendGroupMsg(groupId, e.getMessage(), false);
                log.warn("\t\t  [ExecutorHandler] MsgException - 指令警告: {}", e.getMessage());
            } else
                log.error("\t\t  [ExecutorHandler] MsgException - 群信息获取失败");
        } catch (NullBotLogException e) {
            log.warn("\t\t  [ExecutorHandler] LogException - 指令警告: {}", e.getMessage());
        } catch (Exception e) {
            log.error("\t\t  [ExecutorHandler] Exception - 错误: {}", e.getMessage());
            throw e;
        }

        log.info("\t\t┌─[ExecutorHandler] 执行结束");
    }
}