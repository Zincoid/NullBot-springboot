package com.zincoid.nullbot.core.module.ai.chat.tool.impl;

import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.gateway.processor.CommandEvent;
import com.zincoid.nullbot.bot.gateway.processor.CommandRegistry;
import com.zincoid.nullbot.core.module.ai.chat.plugin.QQCmdAllows;
import com.zincoid.nullbot.core.module.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QQPrivateCmdTool implements Tool {

    private record Args(String command, String args) {}

    private final ToolDef toolDef;

    private final ApplicationEventPublisher eventPublisher;
    private final CommandRegistry commandRegistry;

    public QQPrivateCmdTool(
            ApplicationEventPublisher eventPublisher,
            CommandRegistry commandRegistry
    ) {
        this.eventPublisher = eventPublisher;
        this.commandRegistry = commandRegistry;
        this.toolDef = buildToolDef();
    }

    private ToolDef buildToolDef() {
        Set<String> cmdAllows = QQCmdAllows.getPm();
        Set<String> cmdDescs = cmdAllows.stream()
                .map(commandRegistry::getCommand)
                .map(Command::getHelpForAI).collect(Collectors.toSet());
        return ToolDef.builder("qq_private_command", "执行QQ私聊机器人指令。可用指令详情: " + cmdDescs)
                .addEnum("command", "要调用的QQ指令名称", cmdAllows, true)
                .addString("args", "指令参数，多个参数用空格分隔，无参数则留空")
                .build();
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            Args args = ToolDef.parseArgs(jsonArgs, Args.class);
            Command command = commandRegistry.getCommand(args.command());
            if (command == null)
                return "错误: 指令 " + args.command() + " 不存在";
            String cmdStr = args.command() + (args.args().isEmpty() ? "" : " " + args.args());
            eventPublisher.publishEvent(CommandEvent.of(cmdStr));
            return "指令 " + args.command() + " 执行成功";
        } catch (Exception e) {
            log.warn("◉ [QQGroupCmdTool] 执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }
}
