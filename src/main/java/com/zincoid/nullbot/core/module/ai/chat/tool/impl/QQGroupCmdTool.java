package com.zincoid.nullbot.core.module.ai.chat.tool.impl;

import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.bot.gateway.processor.CmdRegistry;
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
public class QQGroupCmdTool implements Tool {

    private record Args(String command, String args) {}

    private final ToolDef toolDef;

    private final ApplicationEventPublisher eventPublisher;
    private final CmdRegistry cmdRegistry;

    public QQGroupCmdTool(
            ApplicationEventPublisher eventPublisher,
            CmdRegistry cmdRegistry
    ) {
        this.eventPublisher = eventPublisher;
        this.cmdRegistry = cmdRegistry;
        this.toolDef = buildToolDef();
    }

    private ToolDef buildToolDef() {
        Set<String> cmdAllows = QQCmdAllows.getGc();
        Set<String> cmdDescs = cmdAllows.stream()
                .map(cmdRegistry::getCmd)
                .map(Cmd::getHelpForAI).collect(Collectors.toSet());
        return ToolDef.builder("qq_group_command", "执行QQ群聊机器人指令。可用指令详情: " + cmdDescs)
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
            Cmd cmd = cmdRegistry.getCmd(args.command());
            if (cmd == null)
                return "错误: 指令 " + args.command() + " 不存在";
            String cmdStr = args.command() + (args.args().isEmpty() ? "" : " " + args.args());
            eventPublisher.publishEvent(CmdEvent.of(cmdStr));
            return "指令 " + args.command() + " 执行成功";
        } catch (Exception e) {
            log.warn("◉ [QQGroupCmdTool] 执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }
}
