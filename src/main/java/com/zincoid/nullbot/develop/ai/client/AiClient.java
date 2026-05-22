package com.zincoid.nullbot.develop.ai.client;

import com.zincoid.nullbot.develop.ai.message.Message;

public interface AiClient<M extends Message> {

    M call(String chatId, String prompt, M message);
}
