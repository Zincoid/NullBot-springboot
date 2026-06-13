package com.zincoid.nullbot.core.module.ai.chat.model;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.tool.ToolDef;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class ModelReq {

    private final List<Message> messages;
    private final List<ToolDef> tools;
    private final boolean thinking;
    private final int maxTokens;

    public static ModelReq of(List<Message> messages, boolean thinking, int maxTokens) {
        return ModelReq.of(messages, null, thinking, maxTokens);
    }
}
