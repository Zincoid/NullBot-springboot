package org.bot.nullbot.dispatcher;

import com.mikuac.shiro.core.Bot;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class CommandProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(CommandProcessor.class);
    private final CommandRegistry registry;
    private final List<Handler> handlers;

    @Async("virtualThreadExecutor")
    public void processQQ(Bot bot, CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            logger.info("└─[CommandProcessor] 正在处理 {} 命令...", event.getCommandType());
            chainProcess(bot, event, command);
            logger.info("  [CommandProcessor] {} 命令处理完毕", event.getCommandType());
        } else
            logger.info("└─[CommandProcessor] 命令不存在");
    }

    @Async("virtualThreadExecutor")
    public void processTest(CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            logger.info("[CommandProcessor] 正在处理 {} 命令 (TEST)...", event.getCommandType());
            chainProcess(null, event, command);
            logger.info("[CommandProcessor] {} 命令处理完毕", event.getCommandType());
        } else
            logger.info("[CommandProcessor] 命令不存在");
    }

    public void chainProcess(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        CommandHandlerChain chain = new CommandHandlerChain(handlers);
        chain.doHandle(bot, event, command);
    }
}