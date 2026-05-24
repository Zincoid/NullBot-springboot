package com.zincoid.nullbot.core.component.ai.chat.tool;

public interface Tool {

    String execute(String args);

    ToolDef getDef();
}
