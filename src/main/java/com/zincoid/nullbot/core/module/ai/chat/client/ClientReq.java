package com.zincoid.nullbot.core.module.ai.chat.client;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientReq<M extends Message> {

    private final String chatId;
    private final String prompt;
    private final M message;
    private final boolean thinking;
    private final int maxTokens;
}
