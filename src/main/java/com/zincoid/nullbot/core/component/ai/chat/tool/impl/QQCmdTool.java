package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.Event;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandProcessor;
import com.zincoid.nullbot.bot.dispatcher.CommandRegistry;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class QQCmdTool implements Tool {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final CommandProcessor commandProcessor;
    private final CommandRegistry commandRegistry;
    private final Set<String> commandAllowSet;
    private final Set<String> commandNames;

    private final ToolDef toolDef;

    public QQCmdTool(
            CommandProcessor commandProcessor,
            CommandRegistry commandRegistry,
            Set<String> commandAllowSet
    ) {
        this.commandProcessor = commandProcessor;
        this.commandRegistry = commandRegistry;
        this.commandAllowSet = commandAllowSet;
        this.commandNames = commandAllowSet;
        this.toolDef = buildToolDef();
    }

    private ToolDef buildToolDef() {
        ObjectNode params = mapper.createObjectNode();
        params.put("type", "object");

        ObjectNode props = mapper.createObjectNode();

        ObjectNode cmdProp = mapper.createObjectNode();
        cmdProp.put("type", "string");
        cmdProp.put("description", "要调用的QQ指令名称");
        ArrayNode enumNode = mapper.createArrayNode();
        commandAllowSet.stream().sorted().forEach(enumNode::add);
        cmdProp.set("enum", enumNode);
        props.set("command", cmdProp);

        ObjectNode argsProp = mapper.createObjectNode();
        argsProp.put("type", "string");
        argsProp.put("description", "指令参数，多个参数用空格分隔，无参数则留空");
        props.set("args", argsProp);

        params.set("properties", props);

        ArrayNode required = mapper.createArrayNode();
        required.add("command");
        params.set("required", required);

        return new ToolDef("qq_command", "执行QQ机器人指令。可用指令: " + commandNames, params);
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(Object ...args) {
        try {
            JsonNode root = mapper.readTree(arguments);
            String cmdName = root.path("command").asText();
            String argsStr = root.path("args").asText("");

            Command command = commandRegistry.getCommand(cmdName);
            if (command == null) {
                return "错误: 指令 " + cmdName + " 不存在";
            }

            String cmdStr = cmdName + (argsStr.isEmpty() ? "" : " " + argsStr);
            CommandEvent<?> toolEvent = new CommandEvent<>(event, cmdStr, false, false);
            commandProcessor.chainProcess(bot, toolEvent, command);
            return "指令 " + cmdName + " 执行成功";
        } catch (Exception e) {
            log.warn("[QQCmdTool] 执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }
}
