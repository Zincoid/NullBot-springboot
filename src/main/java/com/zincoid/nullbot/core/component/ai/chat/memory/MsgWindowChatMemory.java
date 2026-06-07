package com.zincoid.nullbot.core.component.ai.chat.memory;

import com.zincoid.nullbot.core.component.ai.chat.enums.Role;
import com.zincoid.nullbot.core.component.ai.chat.message.Message;
import com.zincoid.nullbot.core.component.ai.chat.repository.ChatRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MsgWindowChatMemory implements ChatMemory {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final ChatRepository chatRepository;
    private final int windowSize;

    public static Builder builder(ChatRepository chatRepository) {
        return new Builder(chatRepository);
    }

    @RequiredArgsConstructor
    public static class Builder {

        private final ChatRepository chatRepository;
        private int windowSize = 25;

        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        public MsgWindowChatMemory build() {
            return new MsgWindowChatMemory(chatRepository, windowSize);
        }
    }

    private ReentrantLock getLock(String chatId) {
        return locks.computeIfAbsent(chatId, k -> new ReentrantLock());
    }

    @Override
    public void lock(String chatId) {
        getLock(chatId).lock();
    }

    @Override
    public void unlock(String chatId) {
        ReentrantLock lock = locks.get(chatId);
        if (lock != null && lock.isHeldByCurrentThread())
            lock.unlock();
    }

    @Override
    public void add(String chatId, Message message) {
        ReentrantLock lock = getLock(chatId);
        lock.lock();
        try {
            List<Message> messages = chatRepository.get(chatId);
            if (messages.size() > windowSize) {
                do messages.removeFirst();
                while (!messages.isEmpty() && messages.getFirst().getRole() == Role.TOOL);
            }
            messages.add(message);
            chatRepository.update(chatId, messages);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Message> get(String chatId) {
        ReentrantLock lock = getLock(chatId);
        lock.lock();
        try {
            return chatRepository.get(chatId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear(String chatId) {
        ReentrantLock lock = getLock(chatId);
        lock.lock();
        try {
            chatRepository.clear(chatId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reset() {
        chatRepository.reset();
        locks.clear();
    }
}
