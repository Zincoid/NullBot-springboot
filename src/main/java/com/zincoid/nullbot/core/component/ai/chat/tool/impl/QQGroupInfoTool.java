package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.util.BotCtxUtil;

public class QQGroupInfoTool implements Tool {

    private final ToolDef toolDef;

    private final ObjectMapper objectMapper;

    public QQGroupInfoTool() {
        this.objectMapper = new ObjectMapper();
        this.toolDef = buildToolDef();
    }

    private ToolDef buildToolDef() {
        ObjectNode params = objectMapper.createObjectNode();
        params.put("type", "object");
        ObjectNode props = objectMapper.createObjectNode();
        ObjectNode groupIdProp = objectMapper.createObjectNode();
        groupIdProp.put("type", "string");
        groupIdProp.put("description", "群号(可选参数,不设置时查询本群)");
        props.set("groupId", groupIdProp);
        params.set("properties", props);
        return new ToolDef("qq_group_info", "查询QQ群信息", params);
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            JsonNode root = objectMapper.readTree(jsonArgs);
            Long groupId = root.path("groupId").asLong(0L);
            if (groupId == 0) groupId = BotCtxUtil.getGroupId();
            Bot bot = BotCtxUtil.getBot();
            return bot.getGroupInfo(groupId, true).toString();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
