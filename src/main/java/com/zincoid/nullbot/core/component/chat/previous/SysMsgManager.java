package com.zincoid.nullbot.core.component.chat.previous;

import lombok.Data;
import com.zincoid.nullbot.core.properties.DeepSeekProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Data
public class SysMsgManager {

    private final DeepSeekProperties deepSeekProperties;

    private final Map<Long, String> defaultMessages = new ConcurrentHashMap<>();  // 群聊默认提示词
    private final Map<Long, String> customMessages = new ConcurrentHashMap<>();  // 群聊自定提示词
    private final Map<Long, List<String>> longTermGroupMemories = new ConcurrentHashMap<>();  // 群聊长时记忆

    private final Map<Long, String> userMessages = new ConcurrentHashMap<>();  // 私聊提示词
    private final Map<Long, List<String>> longTermUserMemories = new ConcurrentHashMap<>();  // 私聊长时记忆

    private static final int longTermMemoryCapacity = 10;  // 长时记忆容量

    // =================== 提示词功能相关 ===================

    public String getDefaultMessage(Long groupId) { return defaultMessages.computeIfAbsent(groupId, k -> deepSeekProperties.getDefaultSystemMessage()); }
    public void setDefaultMessage(Long groupId, String message) { defaultMessages.put(groupId, message); }
    public String getCustomMessage(Long groupId) { return customMessages.computeIfAbsent(groupId, k -> "你是一个AI助手，名字叫Null。"); }
    public void setCustomMessage(Long groupId, String message) { customMessages.put(groupId, message); }
    public String getUserMessage(Long userId) { return userMessages.computeIfAbsent(userId, k -> deepSeekProperties.getDefaultSystemMessage()); }
    public void setUserMessage(Long userId, String message) { userMessages.put(userId, message); }

    // =================== 长时记忆功能相关 ===================

    public List<String> getLongTermGroupMemory(Long groupId) { return longTermGroupMemories.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>()); }
    public synchronized boolean addLongTermGroupMemory(Long groupId, String memory) {
        List<String> groupMemory = getLongTermGroupMemory(groupId);
        if (groupMemory.size() >= longTermMemoryCapacity) return false;
        return groupMemory.add(memory);
    }
    public String removeLongTermGroupMemory(Long groupId, int i) { return getLongTermGroupMemory(groupId).remove(i); }
    public void clearLongTermGroupMemory(Long groupId) { longTermGroupMemories.remove(groupId); }
    public List<String> getLongTermUserMemory(Long userId) { return longTermUserMemories.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()); }
    public synchronized boolean addLongTermUserMemory(Long userId, String memory) {
        List<String> userMemory = getLongTermUserMemory(userId);
        if (userMemory.size() >= longTermMemoryCapacity) return false;
        return userMemory.add(memory);
    }
    public String removeLongTermUserMemory(Long userId, int i) { return getLongTermUserMemory(userId).remove(i); }
    public void clearLongTermUserMemory(Long userId) { longTermUserMemories.remove(userId); }

    // =================== 重置功能相关 ===================

    public void resetGroup(Long groupId) {
        defaultMessages.remove(groupId);
        customMessages.remove(groupId);
        longTermGroupMemories.remove(groupId);
    }
    public void resetUser(Long userId) {
        userMessages.remove(userId);
        longTermUserMemories.remove(userId);
    }
}
