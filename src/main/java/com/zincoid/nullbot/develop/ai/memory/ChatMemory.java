package com.zincoid.nullbot.develop.ai.memory;

import com.zincoid.nullbot.develop.ai.message.Message;

import java.util.List;

public interface ChatMemory {

    void add(String chatId, Message message);

    List<Message> get(String chatId);

    void clear(String chatId);
}
