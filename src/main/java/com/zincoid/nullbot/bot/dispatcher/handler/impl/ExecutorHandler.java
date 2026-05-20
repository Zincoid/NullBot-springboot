package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.websocket.WebSocketSender;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.entity.CommandEvent;
import com.zincoid.nullbot.bot.exception.NullBotLogException;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(4)
@Component
@RequiredArgsConstructor
public class ExecutorHandler implements Handler {

    private final WebSocketSender webSocketSender;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        log.info("\t\t└─[ExecutorHandler] 执行开始");

        Long groupId = 0L;  // 群号 0 代表私聊
        Long userId = 0L;  // 用户 0 代表群聊
        try {
            if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
                groupId = groupMessageEvent.getGroupId();
                command.execute(bot, groupMessageEvent, event.getCommandParameters());
            } else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent) {
                groupId = pokeNoticeEvent.getGroupId() == null ? 0L : pokeNoticeEvent.getGroupId();
                if (groupId == 0L) userId = pokeNoticeEvent.getUserId();
                command.execute(bot, pokeNoticeEvent, event.getCommandParameters());
            } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent groupMsgDeleteNoticeEvent) {
                groupId = groupMsgDeleteNoticeEvent.getGroupId();
                command.execute(bot, groupMsgDeleteNoticeEvent, event.getCommandParameters());
            } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
                userId = privateMessageEvent.getUserId();
                command.execute(bot, privateMessageEvent, event.getCommandParameters());
            } else
                log.warn("\t\t  [ExecutorHandler] 不支持的事件类型");

        } catch (NullBotMsgException e) {
            if (groupId != 0L) {
                bot.sendGroupMsg(groupId, e.getMessage(), false);
                log.warn("\t\t  [ExecutorHandler] 群聊警告: {}", e.getMessage());
            }
            if (userId != 0L) {
                bot.sendPrivateMsg(userId, e.getMessage(), false);
                log.warn("\t\t  [ExecutorHandler] 私聊警告: {}", e.getMessage());
            }
        } catch (NullBotLogException e) {
            log.warn("\t\t  [ExecutorHandler] 日志警告: {}", e.getMessage());
            webSocketSender.broadcast("WARN", "服务器日志警告: " + e.getMessage());
        } catch (Exception e) {
            log.error("\t\t  [ExecutorHandler] 未知错误: {}", e.getMessage());
            webSocketSender.broadcast("ERROR", "服务器内部错误: " + e.getMessage());
            throw e;
        }

        log.info("\t\t┌─[ExecutorHandler] 执行结束");
    }
}