package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.stereotype.Component;

@Component
public class QQUserInfoTool implements Tool {

    private record Args(long id) {}

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
            Args args = ToolDef.parseArgs(jsonArgs, Args.class);
            if (args.id() == 0) return "未指定QQ号";
            Bot bot = BotCtx.getBot();
            return bot.getStrangerInfo(args.id(), true).toString();
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
