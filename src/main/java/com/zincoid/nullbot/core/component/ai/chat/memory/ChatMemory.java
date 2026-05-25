package com.zincoid.nullbot.core.component.ai.chat.memory;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;

import java.util.List;

public interface ChatMemory {

    void add(String chatId, Message message);

    List<Message> get(String chatId);

    void clear(String chatId);

    void reset();

    void lock(String chatId);

    void unlock(String chatId);
}
