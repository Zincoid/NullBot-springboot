package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.util.BotCtxUtil;

public class QQUserInfoTool implements Tool {

    private final ToolDef toolDef;

    public QQUserInfoTool() {
        this.toolDef = ToolDef.builder("qq_user_info", "查询QQ用户信息")
                .addString("id", "QQ号", true)
                .build();
    }

    @Override
    public ToolDef getDef() {
        return toolDef;
    }

    @Override
    public String execute(String jsonArgs) {
        try {
            JsonNode root = ToolDef.parseArgs(jsonArgs);
            Long userId = root.path("id").asLong(0L);
            if (userId == 0) return "未指定QQ号";
            Bot bot = BotCtxUtil.getBot();
            return bot.getStrangerInfo(userId, true).toString();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
