package com.zincoid.nullbot.core.module.ai.chat.client;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;

public interface AiClient<M extends Message> {

    M call(String chatId, String prompt, M message, boolean thinking, int maxTokens);
}
