package com.zincoid.nullbot.core.component.chat.current.model;

import com.zincoid.nullbot.core.component.chat.current.message.Message;

import java.util.List;

public interface Model {

    public String invoke(List<Message> messages, boolean thinking, int maxTokens);
}
