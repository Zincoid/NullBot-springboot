package com.zincoid.nullbot.core.component.chat.current.repository;

import com.zincoid.nullbot.core.component.chat.current.message.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
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
