package com.zincoid.nullbot.core.module.ai.chat.tool;

public interface Tool {

    String execute(String jsonArgs);

    ToolDef getDef();
}
