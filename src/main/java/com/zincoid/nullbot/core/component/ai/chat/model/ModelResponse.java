package com.zincoid.nullbot.core.component.ai.chat.model;

import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;
import lombok.Getter;

import java.util.List;

@Getter
public class ModelResponse {

    private final List<ToolCall> toolCalls;
    private final String content;

    private ModelResponse(List<ToolCall> toolCalls, String content) {
        this.toolCalls = toolCalls;
        this.content = content;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public static ModelResponse of(String content) {
        return new ModelResponse(List.of(), content);
    }

    public static ModelResponse of(List<ToolCall> toolCalls) {
        return new ModelResponse(toolCalls, null);
    }
}
