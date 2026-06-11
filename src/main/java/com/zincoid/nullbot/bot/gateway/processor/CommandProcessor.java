package com.zincoid.nullbot.bot.gateway.processor;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.context.BotCtx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.gateway.handler.Handler;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandProcessor {

    private final CommandRegistry registry;
    private final List<Handler> handlers;

    /** 处理用户指令 */
    public void processQQ(Bot bot, CommandEvent<?> event) throws Exception {
        doProcess("▶ [CommandProcessor::QQ]", bot, event);
    }

    /** 处理内部指令 */
    @EventListener
    public void processInner(CommandEvent<?> event) throws Exception {
        doProcess("▷ [CommandProcessor::Inner]", BotCtx.getBot(), event);
    }

    /** 处理测试指令 */
    public void processTest(CommandEvent<?> event) throws Exception {
        doProcess("▶ [CommandProcessor::Test]", null, event);
    }

    // =========== 工具 ===========

    private void doProcess(String tag, Bot bot, CommandEvent<?> event) throws Exception {
        if (tag == null) tag = "▶ [CommandProcessor]";
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("{} {} 指令处理中...", tag, event.getCommandType());
            chainProcess(bot, event, command);
            log.info("{} {} 指令已处理", tag, event.getCommandType());
        } else log.info("{} 指令不存在", tag);
    }

    public void chainProcess(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        CommandHandlerChain chain = new CommandHandlerChain(handlers);
        chain.doHandle(bot, event, command);
    }
}
