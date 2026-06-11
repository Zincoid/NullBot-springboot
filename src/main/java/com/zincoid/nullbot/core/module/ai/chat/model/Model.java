package com.zincoid.nullbot.core.module.ai.chat.model;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolDef;

import java.util.List;

public interface Model {

    default ModelResponse invoke(
            List<Message> messages, List<ToolDef> tools, boolean thinking, int maxTokens
    ) {
        return invoke(messages, thinking, maxTokens);
    }

    ModelResponse invoke(List<Message> messages, boolean thinking, int maxTokens);
}
