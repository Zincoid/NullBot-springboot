package com.zincoid.nullbot.core.component.chat.current.client;

import com.zincoid.nullbot.core.component.chat.current.message.Message;

public interface AiClient<M extends Message> {

    M call(String chatId, String prompt, M message);
}
