package com.zincoid.nullbot.core.component.ai.chat.repository;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;

import java.util.List;

public interface Repository {

    List<Message> get(String chatId);

    void update(String chatId, List<Message> messages);

    void clear(String chatId);

    void reset();
}
