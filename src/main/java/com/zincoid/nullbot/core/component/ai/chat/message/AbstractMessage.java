package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;

import java.util.List;

public abstract class AbstractMessage implements Message {

    protected final Role role;
    protected final String content;
    protected final String toolCallId;
    protected final List<ToolCall> toolCalls;

    protected AbstractMessage(Role role, String content, String toolCallId, List<ToolCall> toolCalls) {
        this.role = role;
        this.content = content;
        this.toolCallId = toolCallId;
        this.toolCalls = toolCalls;
    }

    @Override
    public Role getRole() {
        return role;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public String getToolCallId() {
        return toolCallId;
    }

    @Override
    public List<ToolCall> getToolCalls() {
        return toolCalls;
    }
}
