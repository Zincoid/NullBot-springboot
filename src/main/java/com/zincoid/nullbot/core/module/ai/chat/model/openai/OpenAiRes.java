package com.zincoid.nullbot.core.module.ai.chat.model.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolCall;

import java.util.List;
import java.util.Optional;

public record OpenAiRes(List<Choice> choices) {

    public record Choice(Message message) {}
    public record Message(String content, @JsonProperty("reasoning_content") String reasoningContent, @JsonProperty("tool_calls") List<ToolCallNode> toolCalls) {}
    public record ToolCallNode(String id, Function function) {}
    public record Function(String name, String arguments) {}

    public ModelRes toModelRes() {
        Message msg = Optional.ofNullable(choices)
                .filter(c -> !c.isEmpty())
                .map(c -> c.getFirst().message)
                .orElse(null);
        if (msg == null)
            throw new RuntimeException("无可用响应消息");
        String content = msg.content != null ? msg.content : "";
        String reasoning = msg.reasoningContent != null ? msg.reasoningContent : "";
        if (msg.toolCalls == null || msg.toolCalls.isEmpty())
            return ModelRes.of(content, reasoning);
        List<ToolCall> calls = msg.toolCalls.stream()
                .map(tc -> new ToolCall(tc.id, tc.function.name, tc.function.arguments))
                .toList();
        return ModelRes.of(calls, reasoning);
    }
}
