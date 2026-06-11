package com.zincoid.nullbot.core.module.ai.chat.memory;

import com.zincoid.nullbot.core.module.ai.chat.message.Message;

import java.util.List;

public interface Memory {

    void add(String chatId, Message message);

    List<Message> get(String chatId);

    void clear(String chatId);

    void reset();

    void lock(String chatId);

    void unlock(String chatId);
}
