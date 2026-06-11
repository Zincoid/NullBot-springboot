package com.zincoid.nullbot.core.module.control;

import com.zincoid.nullbot.core.properties.ai.AiChatProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Data
public class SysMsgManager {

    private static final int MEMORY_CAPACITY = 10;  // 记忆容量

    private final Map<Long, String> groupMessages = new ConcurrentHashMap<>();  // 群聊提示词
    private final Map<Long, String> userMessages = new ConcurrentHashMap<>();  // 私聊提示词
    private final Map<Long, List<String>> groupMemories = new ConcurrentHashMap<>();  // 群聊记忆
    private final Map<Long, List<String>> userMemories = new ConcurrentHashMap<>();  // 私聊记忆

    private final AiChatProperties aiChatProperties;

    // =================== 提示词功能相关 ===================

    public String getGroupMessage(Long groupId) { return groupMessages.computeIfAbsent(groupId, k -> aiChatProperties.getDefaultSysMsg()); }
    public String getUserMessage(Long userId) { return userMessages.computeIfAbsent(userId, k -> aiChatProperties.getDefaultSysMsg()); }
    public void setGroupMessage(Long groupId, String message) { groupMessages.put(groupId, message); }
    public void setUserMessage(Long userId, String message) { userMessages.put(userId, message); }

    // ==================== 记忆功能相关 ====================

    public List<String> getGroupMemory(Long groupId) { return groupMemories.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>()); }
    public List<String> getUserMemory(Long userId) { return userMemories.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()); }
    public synchronized boolean addGroupMemory(Long groupId, String memory) {
        List<String> groupMemory = getGroupMemory(groupId);
        if (groupMemory.size() >= MEMORY_CAPACITY) return false;
        return groupMemory.add(memory);
    }
    public synchronized boolean addUserMemory(Long userId, String memory) {
        List<String> userMemory = getUserMemory(userId);
        if (userMemory.size() >= MEMORY_CAPACITY) return false;
        return userMemory.add(memory);
    }
    public String removeGroupMemory(Long groupId, int i) { return getGroupMemory(groupId).remove(i); }
    public String removeUserMemory(Long userId, int i) { return getUserMemory(userId).remove(i); }
    public void clearGroupMemory(Long groupId) { groupMemories.remove(groupId); }
    public void clearUserMemory(Long userId) { userMemories.remove(userId); }

    // =================== 重置功能相关 ===================

    public void resetGroup(Long groupId) {
        groupMessages.remove(groupId);
        groupMemories.remove(groupId);
    }
    public void resetUser(Long userId) {
        userMessages.remove(userId);
        userMemories.remove(userId);
    }
}
