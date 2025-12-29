package org.bot.nullbot.dispatcher;

import com.mikuac.shiro.core.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.EmbeddedCommandEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandProcessor
{
    private final CommandRegistry registry;
    private final List<Handler> handlers;

    @Async("ThreadExecutor")
    public void processQQ(Bot bot, CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("└─[CommandProcessor] 正在处理 {} 命令...", event.getCommandType());
            chainProcess(bot, event, command);
            log.info("  [CommandProcessor] {} 命令处理完毕", event.getCommandType());
        } else
            log.info("└─[CommandProcessor] 命令不存在");
    }

    // 同步处理嵌入指令
    @EventListener
    public void processEmbeddedQQ(EmbeddedCommandEvent embeddedEvent) throws Exception {
        String commandType = embeddedEvent.getEvent().getCommandType();
        Command command = registry.getCommand(commandType);
        if (command != null) {
            log.info("  [CommandProcessor-Embed] 正在处理内嵌 {} 命令...", commandType);
            chainProcess(embeddedEvent.getBot(), embeddedEvent.getEvent(), command);
            log.info("  [CommandProcessor-Embed] 内嵌 {} 命令处理完毕", commandType);
        } else
            log.info("  [CommandProcessor-Embed] 内嵌命令不存在");
    }

    @Async("ThreadExecutor")
    public void processTest(CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("[CommandProcessor] 正在处理 {} 命令 (TEST)...", event.getCommandType());
            chainProcess(null, event, command);
            log.info("[CommandProcessor] {} 命令处理完毕", event.getCommandType());

            // 在Command组件中使用
            // if(bot == null){
            //     logger.info("[Test] 测试结果");
            // }

        } else
            log.info("[CommandProcessor] 命令不存在");
    }

    public void chainProcess(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        CommandHandlerChain chain = new CommandHandlerChain(handlers);
        chain.doHandle(bot, event, command);
    }
}