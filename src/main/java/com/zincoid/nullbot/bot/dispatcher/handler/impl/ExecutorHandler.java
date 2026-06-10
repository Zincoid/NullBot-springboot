package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotOmitException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.util.DownloadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.WsSender;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.enums.EventScope;
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

        // String commandClassName = command.getClass().getSimpleName();          // 指令类名
        CommandArgs params = new CommandArgs(event.getCommandParameters());    // 包装参数
        EventScope eventScope = event.getEventScope();
        Long groupId = eventScope == EventScope.GROUP ? event.getGroupId() : 0L;  // 群组ID - 0 代表私聊
        Long userId = eventScope == EventScope.PRIVATE ? event.getUserId() : 0L;  // 用户ID - 0 代表群聊

        try {
            if (event.getEvent() instanceof GroupMessageEvent _event) {
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof PrivateMessageEvent _event) {
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof PokeNoticeEvent _event) {
                command.execute(bot, _event, params);
            } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent _event) {
                command.execute(bot, _event, params);
            } else log.warn("  [ExecutorHandler] 未知事件不可执行");

        } catch (BotWarnException e) {
            log.warn("  [ExecutorHandler] 告警异常: {}", e.getMessage());
            sendError(bot, groupId, userId, "⚠️Warn: %s".formatted(e.getMessage()));
        } catch (BotInfoException e) {
            log.info("  [ExecutorHandler] 提示异常: {}", e.getMessage());
            sendError(bot, groupId, userId, e.getMessage());
        } catch (BotOmitException e) {
            log.info("  [ExecutorHandler] 忽略异常: {}", e.getMessage());
            wsSender.broadcast("INFO", "忽略异常: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("  [ExecutorHandler] 严重异常: {}", e.getMessage());
            sendError(bot, groupId, userId, "❌Error: %s".formatted(e.getMessage()));
            wsSender.broadcast("ERROR", "严重异常: " + e.getMessage());
            throw e;

        } finally {
            int tempCount = DownloadUtil.cleanup();
            log.info("  [ExecutorHandler] 临时文件已清理: {}", tempCount);
        }

        log.info("┌─[ExecutorHandler] 执行结束");
    }

    private void sendError(Bot bot, Long groupId, Long userId, String message) {
        if (groupId != 0L) bot.sendGroupMsg(groupId, message, false);
        if (userId != 0L) bot.sendPrivateMsg(userId, message, false);
    }
}