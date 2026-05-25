package com.zincoid.nullbot.bot.dispatcher;

import com.mikuac.shiro.core.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.model.bot.event.InnerCommandEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandProcessor {

    private final CommandRegistry registry;
    private final List<Handler> handlers;

    // 处理用户指令
    public void processQQ(Bot bot, CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("└─[CommandProcessor::QQ] 正在处理 {} 命令...", event.getCommandType());
            chainProcess(bot, event, command);
            log.info("■ [CommandProcessor::QQ] {} 命令处理完毕", event.getCommandType());
        } else
            log.info("■ [CommandProcessor::QQ] 命令不存在");
    }

    // 处理内部指令
    @EventListener
    public void processInner(InnerCommandEvent event) throws Exception {
        Command command = registry.getCommand(event.getEvent().getCommandType());
        if (command != null) {
            log.info("\t\t▶ [CommandProcessor::Inner] 正在处理 {} 命令...", event.getEvent().getCommandType());
            chainProcess(event.getBot(), event.getEvent(), command);
            log.info("\t\t■ [CommandProcessor::Inner] {} 命令处理完毕", event.getEvent().getCommandType());
        } else
            log.info("\t\t■ [CommandProcessor::Inner] 命令不存在");
    }

    // 处理测试指令
    public void processTest(CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("▶ [CommandProcessor::Test] 正在处理 {} 命令...", event.getCommandType());
            chainProcess(null, event, command);
            log.info("■ [CommandProcessor::Test] {} 命令处理完毕", event.getCommandType());
        } else
            log.info("■ [CommandProcessor::Test] 命令不存在");
    }

    public void chainProcess(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        CommandHandlerChain chain = new CommandHandlerChain(handlers);
        chain.doHandle(bot, event, command);
    }
}
