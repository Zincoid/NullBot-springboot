package com.zincoid.nullbot.develop.ai.model;

import com.zincoid.nullbot.develop.ai.message.Message;

import java.util.List;

public interface Model {

    public String invoke(List<Message> messages, boolean thinking, int maxTokens);
}
