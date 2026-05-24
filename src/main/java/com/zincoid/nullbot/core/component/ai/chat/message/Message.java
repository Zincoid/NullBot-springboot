package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;

import java.util.List;
import java.util.Map;

public interface Message {

    Role getRole();

    String getContent();

    default List<ToolCall> getToolCalls() { return List.of(); }

    Map<String, Object> toMap();
}
