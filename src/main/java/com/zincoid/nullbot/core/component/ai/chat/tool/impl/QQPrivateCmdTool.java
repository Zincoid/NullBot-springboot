package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandRegistry;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
import com.zincoid.nullbot.core.model.bot.event.EmbeddedCommandEvent;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QQPrivateCmdTool implements Tool {

    private final ToolDef toolDef;

    private final ApplicationEventPublisher eventPublisher;
    private final CommandRegistry commandRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> PM_CMD_ALLOWS;

    static {

        PM_CMD_ALLOWS = Set.of(
                /* ========== 普通命令 ========== */
                "Help",
                /* ========== 合成命令 ========== */
                "Tts",
                /* ========== 加密命令 ========== */
                "65275d24", "0167a25a", "bab329aa"
        );

    }

    public QQPrivateCmdTool(
            ApplicationEventPublisher eventPublisher,
            CommandRegistry commandRegistry
    ) {
        this.eventPublisher = eventPublisher;
        this.commandRegistry = commandRegistry;
        this.toolDef = buildToolDef(PM_CMD_ALLOWS.stream()
                .map(commandRegistry::getCommand)
                .map(Command::getHelpForAI).collect(Collectors.toSet()));
    }

    private ToolDef buildToolDef(Set<String> commandDescs) {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");

        ObjectNode props = objectMapper.createObjectNode();

        ObjectNode cmdProp = objectMapper.createObjectNode();
        cmdProp.put("type", "string");
        cmdProp.put("description", "要调用的QQ指令名称");
        ArrayNode enumNode = objectMapper.createArrayNode();
        PM_CMD_ALLOWS.stream().sorted().forEach(enumNode::add);
        cmdProp.set("enum", enumNode);
        props.set("command", cmdProp);

        ObjectNode argsProp = objectMapper.createObjectNode();
        argsProp.put("type", "string");
        argsProp.put("description", "指令参数，多个参数用空格分隔，无参数则留空");
        props.set("args", argsProp);

        params.set("properties", props);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("command");
        params.set("required", required);

        return new ToolDef(
                "qq_private_command",
                "执行QQ私聊机器人指令。可用指令详情: " + commandDescs,
                params
        );
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            JsonNode root = objectMapper.readTree(jsonArgs);
            String cmdName = root.path("command").asText();
            String argsStr = root.path("args").asText("");

            Command command = commandRegistry.getCommand(cmdName);
            if (command == null)
                return "错误: 指令 " + cmdName + " 不存在";

            String cmdStr = cmdName + (argsStr.isEmpty() ? "" : " " + argsStr);
            eventPublisher.publishEvent(
                    new EmbeddedCommandEvent(
                            BotCtxUtil.getBot(),
                            new CommandEvent<>(BotCtxUtil.getEvent(), cmdStr, false, false)
                    )
            );
            return "指令 " + cmdName + " 执行成功";

        } catch (Exception e) {
            log.warn("◉ [QQGroupCmdTool] 执行失败: {}", e.getMessage());
            return "错误: " + e.getMessage();
        }
    }
}
