package com.zincoid.nullbot.core.component.ai.chat.memory;

import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class MsgWindowChatMemory implements ChatMemory {

    private final ChatRepository chatRepository;
    private final int windowSize;

    @Override
    public void add(String chatId, Message message) {
        List<Message> messages = chatRepository.get(chatId);
        if (messages.size() > windowSize)
            messages.removeFirst();
        messages.add(message);
        chatRepository.update(chatId, messages);
    }

    @Override
    public List<Message> get(String chatId) {
        return chatRepository.get(chatId);
    }

    @Override
    public void clear(String chatId) {
        chatRepository.clear(chatId);
    }
}
