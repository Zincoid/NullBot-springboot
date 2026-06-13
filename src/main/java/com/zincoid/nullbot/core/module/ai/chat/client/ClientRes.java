package com.zincoid.nullbot.core.module.ai.chat.client;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ClientRes<M extends Message> {

    private final M message;

    public String getContent() {
        return message.getContent();
    }
}
