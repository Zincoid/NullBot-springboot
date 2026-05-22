package com.zincoid.nullbot.develop.ai.repository;

import com.zincoid.nullbot.develop.ai.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryChatRepository implements ChatRepository {

    private final Map<String, List<Message>> data = new ConcurrentHashMap<>();

    @Override
    public List<Message> get(String chatId) {
        List<Message> messages = data.get(chatId);
        return messages != null ? new ArrayList<>(messages) : new ArrayList<>();
    }

    @Override
    public void update(String chatId, List<Message> messages) {
        data.put(chatId, messages);
    }

    @Override
    public void clear(String chatId) {
        data.remove(chatId);
    }
}
