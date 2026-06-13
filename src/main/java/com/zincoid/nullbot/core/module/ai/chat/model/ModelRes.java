package com.zincoid.nullbot.core.module.ai.chat.model;

import com.zincoid.nullbot.core.module.ai.chat.tool.ToolCall;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class ModelRes {

    private final List<ToolCall> toolCalls;
    private final String content;
    private final String reasoningContent;

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    public static ModelRes of(String content, String reasoningContent) {
        return ModelRes.of(List.of(), content, reasoningContent);
    }

    public static ModelRes of(List<ToolCall> toolCalls, String reasoningContent) {
        return ModelRes.of(toolCalls, null, reasoningContent);
    }
}
