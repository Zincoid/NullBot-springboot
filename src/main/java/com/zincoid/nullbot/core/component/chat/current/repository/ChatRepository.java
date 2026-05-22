package com.zincoid.nullbot.core.component.chat.current.repository;

import com.zincoid.nullbot.core.component.chat.current.message.Message;

import java.util.List;

public interface ChatRepository {

    List<Message> get(String chatId);

    void update(String chatId, List<Message> messages);

    void clear(String chatId);
}
