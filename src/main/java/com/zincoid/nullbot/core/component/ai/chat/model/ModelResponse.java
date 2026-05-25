package com.zincoid.nullbot.core.component.ai.chat.model;

import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;
import lombok.Getter;

import java.util.List;

@Getter
public class ModelResponse {

    private final List<ToolCall> toolCalls;
    private final String content;
    private final String reasoningContent;

    private ModelResponse(List<ToolCall> toolCalls, String content, String reasoningContent) {
        this.toolCalls = toolCalls;
        this.content = content;
        this.reasoningContent = reasoningContent;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public static ModelResponse of(String content, String reasoningContent) {
        return new ModelResponse(List.of(), content, reasoningContent);
    }

    public static ModelResponse of(List<ToolCall> toolCalls, String reasoningContent) {
        return new ModelResponse(toolCalls, null, reasoningContent);
    }
}
