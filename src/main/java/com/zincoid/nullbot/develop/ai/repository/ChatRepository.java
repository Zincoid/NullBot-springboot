package com.zincoid.nullbot.develop.ai.repository;

import com.zincoid.nullbot.develop.ai.message.Message;

import java.util.List;

public interface ChatRepository {

    List<Message> get(String chatId);

    void update(String chatId, List<Message> messages);

    void clear(String chatId);
}
