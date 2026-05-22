package com.zincoid.nullbot.core.component.chat.current.memory;

import com.zincoid.nullbot.core.component.chat.current.message.Message;

import java.util.List;

public interface ChatMemory {

    void add(String chatId, Message message);

    List<Message> get(String chatId);

    void clear(String chatId);
}
