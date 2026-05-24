package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.util.BotCtxUtil;

public class QQUserInfoTool implements Tool {

    private final ToolDef toolDef;

    private final ObjectMapper objectMapper;

    public QQUserInfoTool() {
        this.objectMapper = new ObjectMapper();
        this.toolDef = buildToolDef();
    }

    private ToolDef buildToolDef() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        ObjectNode props = objectMapper.createObjectNode();
        ObjectNode idProp = objectMapper.createObjectNode();
        idProp.put("type", "string");
        idProp.put("description", "QQ号");
        props.set("id", idProp);
        params.set("properties", props);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("userId");
        params.set("required", required);
        return new ToolDef("qq_user_info", "查询QQ用户信息", params);
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            JsonNode root = objectMapper.readTree(jsonArgs);
            Long userId = root.path("id").asLong(0L);
            if (userId == 0) return "未指定QQ号";
            Bot bot = BotCtxUtil.getBot();
            return bot.getStrangerInfo(userId, true).toString();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
