package com.zincoid.nullbot.core.component.ai.chat.tool.impl;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.ai.chat.tool.Tool;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.stereotype.Component;

@Component
public class QQGroupInfoTool implements Tool {

    private record Args(long id) {}

    private final ToolDef toolDef;

    public QQGroupInfoTool() {
        this.toolDef = ToolDef.builder("qq_group_info", "查询QQ群信息")
                .addString("id", "群号(可选参数,不设置时查询本群)")
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
            long groupId = args.id() == 0 ? BotCtx.getGroupId() : args.id();
            Bot bot = BotCtx.getBot();
            String groupInfo = bot.getGroupInfo(groupId, true).toString();
            String groupUsers = bot.getGroupMemberList(groupId).toString();
            return "群信息: " + groupInfo + " 群成员: " + groupUsers;
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
}
