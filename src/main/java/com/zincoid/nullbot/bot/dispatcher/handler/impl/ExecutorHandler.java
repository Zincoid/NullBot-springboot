package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.WsSender;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(4)
@Component
@RequiredArgsConstructor
public class ExecutorHandler implements Handler {

    private final WsSender wsSender;

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        log.info("└─[ExecutorHandler] 执行开始");

        String commandClassName = command.getClass().getSimpleName();        // 指令类名
        CommandArgs params = new CommandArgs(event.getCommandParameters());  // 包装参数
        Long groupId = 0L;                                                   // 群组ID - 0 代表私聊
        Long userId = 0L;                                                    // 用户ID - 0 代表群聊

        try {
            if (event.getEvent() instanceof GroupMessageEvent _event) {
                groupId = _event.getGroupId();
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof PokeNoticeEvent _event) {
                groupId = _event.getGroupId() == null ? 0L : _event.getGroupId();
                if (groupId == 0L) userId = _event.getUserId();
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent _event) {
                groupId = _event.getGroupId();
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof PrivateMessageEvent _event) {
                userId = _event.getUserId();
                command.execute(bot, _event, params);
            } else log.warn("  [ExecutorHandler] 不支持的事件类型");

        } catch (NullBotException e) {
            log.warn("  [ExecutorHandler] 指令警告: {}", e.getMessage());
            String message = "[%s] Warn: %s".formatted(commandClassName, e.getMessage());
            if (groupId != 0L) bot.sendGroupMsg(groupId, message, false);
            if (userId != 0L) bot.sendPrivateMsg(userId, message, false);

        } catch (Exception e) {
            log.error("  [ExecutorHandler] 指令错误: {}", e.getMessage());
            String message = "[%s] Error: %s".formatted(commandClassName, e.getMessage());
            if (groupId != 0L) bot.sendGroupMsg(groupId, message, false);
            if (userId != 0L) bot.sendPrivateMsg(userId, message, false);
            wsSender.broadcast("ERROR", "服务器内部错误: " + e.getMessage());
            throw e;
        }

        log.info("┌─[ExecutorHandler] 执行结束");
    }
}