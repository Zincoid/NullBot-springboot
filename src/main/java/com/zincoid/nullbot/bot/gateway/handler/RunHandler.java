package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotOmitException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.utils.DownloadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.system.WsSender;
import com.zincoid.nullbot.bot.gateway.processor.CmdHandlerChain;
import com.zincoid.nullbot.core.enums.EventScope;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(4)
@Component
@RequiredArgsConstructor
public class RunHandler implements Handler {

    private final WsSender wsSender;

    @Override
    public void handle(Bot bot, Cmd cmd, CmdEvent<?> event, CmdHandlerChain chain) throws Exception {
        log.info("└─[RunHandler] 执行开始");

        // String cmdClsName = cmd.getClass().getSimpleName();               // 指令类名
        CmdArgs args = CmdArgs.of(event.getCmdParams());                     // 指令参数
        EventScope scope = event.getEventScope();                            // 指令作用域
        Long groupId = scope == EventScope.GROUP ? event.getGroupId() : 0L;  // 群组ID - 私聊置 0
        Long userId = scope == EventScope.PRIVATE ? event.getUserId() : 0L;  // 用户ID - 群聊置 0

        try {
            if (event.getEvent() instanceof GroupMessageEvent _event) {
                cmd.run(bot, _event, args);
            } else if (event.getEvent() instanceof PrivateMessageEvent _event) {
                cmd.run(bot, _event, args);
            } else if (event.getEvent() instanceof PokeNoticeEvent _event) {
                cmd.run(bot, _event, args);
            } else if (event.getEvent() instanceof GroupMsgDeleteNoticeEvent _event) {
                cmd.run(bot, _event, args);
            } else log.warn("  [RunHandler] 未知事件不可执行");

        } catch (BotWarnException e) {
            log.warn("  [RunHandler] 告警异常: {}", e.getMessage());
            sendError(bot, groupId, userId, "⚠️Warn: %s".formatted(e.getMessage()));
        } catch (BotInfoException e) {
            log.info("  [RunHandler] 提示异常: {}", e.getMessage());
            sendError(bot, groupId, userId, e.getMessage());
        } catch (BotOmitException e) {
            log.info("  [RunHandler] 忽略异常: {}", e.getMessage());
            wsSender.broadcast("INFO", "忽略异常: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("  [RunHandler] 严重异常: {}", e.getMessage());
            sendError(bot, groupId, userId, "❌Error: %s".formatted(e.getMessage()));
            wsSender.broadcast("ERROR", "严重异常: " + e.getMessage());
            throw e;

        } finally {
            int tempCount = DownloadUtil.cleanup();
            log.info("  [RunHandler] 临时文件已清理: {}", tempCount);
        }

        log.info("┌─[RunHandler] 执行结束");
    }

    private void sendError(Bot bot, Long groupId, Long userId, String message) {
        if (groupId != 0L) bot.sendGroupMsg(groupId, message, false);
        if (userId != 0L) bot.sendPrivateMsg(userId, message, false);
    }
}