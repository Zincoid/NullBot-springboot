package com.zincoid.nullbot.core.module.ai.chat.memory;

import com.zincoid.nullbot.core.enums.Role;
import com.zincoid.nullbot.core.module.ai.chat.message.Message;
import com.zincoid.nullbot.core.module.ai.chat.repository.Repository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MsgWindowMemory implements Memory {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    private final Repository repository;
    private final int windowSize;

    public static Builder builder(Repository repository) {
        return new Builder(repository);
    }

    @RequiredArgsConstructor
    public static class Builder {

        private final Repository repository;
        private int windowSize = 25;

        public Builder windowSize(int windowSize) {
            this.windowSize = windowSize;
            return this;
        }

        public MsgWindowMemory build() {
            return new MsgWindowMemory(repository, windowSize);
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
            List<Message> messages = repository.get(chatId);
            if (messages.size() > windowSize) {
                do messages.removeFirst();
                while (!messages.isEmpty() && messages.getFirst().getRole() == Role.TOOL);
            }
            messages.add(message);
            repository.update(chatId, messages);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Message> get(String chatId) {
        ReentrantLock lock = getLock(chatId);
        lock.lock();
        try {
            return repository.get(chatId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear(String chatId) {
        ReentrantLock lock = getLock(chatId);
        lock.lock();
        try {
            repository.clear(chatId);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void reset() {
        repository.reset();
        locks.clear();
    }
}
