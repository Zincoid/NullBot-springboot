package com.zincoid.nullbot.core.component.ai.chat.model;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.tool.ToolDef;

import java.util.List;

public interface Model {

    ModelResponse invoke(List<Message> messages, boolean thinking, int maxTokens);

    default ModelResponse invoke(List<Message> messages, List<ToolDef> tools, boolean thinking, int maxTokens) {
        return invoke(messages, thinking, maxTokens);
    }
}
