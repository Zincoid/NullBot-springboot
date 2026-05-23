package com.zincoid.nullbot.core.component.ai.chat.model;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;

import java.util.List;

public interface Model {

    public Message invoke(List<Message> messages, boolean thinking, int maxTokens);
}
