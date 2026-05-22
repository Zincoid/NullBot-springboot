package com.zincoid.nullbot.core.component.ai.chat.client;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;

public interface AiClient<M extends Message> {

    M call(String chatId, String prompt, M message, boolean thinking, int maxTokens);
}
