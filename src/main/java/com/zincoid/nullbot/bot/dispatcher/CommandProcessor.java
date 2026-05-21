package com.zincoid.nullbot.bot.dispatcher;

import com.mikuac.shiro.core.Bot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.CommandEvent;
import com.zincoid.nullbot.core.model.bot.EmbeddedCommandEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandProcessor {

    private final CommandRegistry registry;
    private final List<Handler> handlers;

    // 处理普通消息指令
    public void processQQ(Bot bot, CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("└─[CommandProcessor] 正在处理 {} 命令...", event.getCommandType());
            chainProcess(bot, event, command);
            log.info("  [CommandProcessor] {} 命令处理完毕", event.getCommandType());
        } else
            log.info("└─[CommandProcessor] 命令不存在");
    }

    // 监听处理嵌入指令
    @EventListener
    public void processEmbeddedQQ(EmbeddedCommandEvent embeddedEvent) throws Exception {
        String commandType = embeddedEvent.getEvent().getCommandType();
        Command command = registry.getCommand(commandType);
        if (command != null) {
            log.info("\t\t◉ [CommandProcessor-Embed] 正在处理内嵌 {} 命令...", commandType);
            // Thread.sleep(500);  // 防止触发间隔限制 (已优化 - 忽略速率限制)
            chainProcess(embeddedEvent.getBot(), embeddedEvent.getEvent(), command);
            log.info("\t\t◉ [CommandProcessor-Embed] 内嵌 {} 命令处理完毕", commandType);
        } else
            log.info("\t\t◉ [CommandProcessor-Embed] 内嵌命令不存在");
    }

    public void processTest(CommandEvent<?> event) throws Exception {
        Command command = registry.getCommand(event.getCommandType());
        if (command != null) {
            log.info("[CommandProcessor] 正在处理 {} 命令 (TEST)...", event.getCommandType());
            chainProcess(null, event, command);
            log.info("[CommandProcessor] {} 命令处理完毕", event.getCommandType());

            // 在 Command 组件中使用
            // if (bot == null) {
            //     log.info("[Test] 测试结果");
            // }

        } else
            log.info("[CommandProcessor] 命令不存在");
    }

    public void chainProcess(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        CommandHandlerChain chain = new CommandHandlerChain(handlers);
        chain.doHandle(bot, event, command);
    }
}
