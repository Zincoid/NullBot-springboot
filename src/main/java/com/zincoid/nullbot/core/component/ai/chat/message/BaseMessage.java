package com.zincoid.nullbot.core.component.ai.chat.message;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolCall;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ToString(callSuper = true)
public class BaseMessage extends AbstractMessage {

    private String reasoningContent;
    private final List<ToolCall> toolCalls;
    private final String toolCallId;

    private BaseMessage(Role role, String content, String reasoningContent, List<ToolCall> toolCalls, String toolCallId) {
        super(role, content);
        this.reasoningContent = reasoningContent;
        this.toolCalls = toolCalls;
        this.toolCallId = toolCallId;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("role", super.role.getValue());
        if (super.content != null && !super.content.isEmpty()) {
            map.put("content", super.content);
        }
        if (reasoningContent != null && !reasoningContent.isEmpty()) {
            map.put("reasoning_content", reasoningContent);
        }
        if (toolCalls != null && !toolCalls.isEmpty()) {
            List<Map<String, Object>> tcList = new ArrayList<>();
            for (ToolCall tc : toolCalls) {
                Map<String, Object> tcMap = new HashMap<>();
                tcMap.put("id", tc.getId());
                tcMap.put("type", "function");
                tcMap.put("function", Map.of("name", tc.getName(), "arguments", tc.getArguments()));
                tcList.add(tcMap);
            }
            map.put("tool_calls", tcList);
        }
        if (toolCallId != null) {
            map.put("tool_call_id", toolCallId);
        }
        return map;
    }

    // ==================== 构建方法 ====================

    public BaseMessage withReasoning(String content) {
        this.reasoningContent = content;
        return this;
    }

    public static BaseMessage user(String content) {
        return new BaseMessage(Role.USER, content, null, null, null);
    }
    public static BaseMessage assistant(String content) {
        return new BaseMessage(Role.ASSISTANT, content, null, null, null);
    }
    public static BaseMessage assistant(List<ToolCall> toolCalls) {
        return new BaseMessage(Role.ASSISTANT, null, null, toolCalls, null);
    }
    public static BaseMessage system(String content) {
        return new BaseMessage(Role.SYSTEM, content, null, null, null);
    }
    public static BaseMessage tool(String toolCallId, String content) {
        return new BaseMessage(Role.TOOL, content, null, null, toolCallId);
    }
}
