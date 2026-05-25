package com.zincoid.nullbot.core.component.control;

import com.zincoid.nullbot.core.properties.AiChatProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Data
public class SysMsgManager {

    private final AiChatProperties aiChatProperties;

    private final Map<Long, String> groupMessages = new ConcurrentHashMap<>();  // 群聊提示词
    private final Map<Long, List<String>> longTermGroupMemories = new ConcurrentHashMap<>();  // 群聊长时记忆
    private final Map<Long, String> userMessages = new ConcurrentHashMap<>();  // 私聊提示词
    private final Map<Long, List<String>> longTermUserMemories = new ConcurrentHashMap<>();  // 私聊长时记忆

    private static final int longTermMemoryCapacity = 10;  // 长时记忆容量

    // =================== 提示词功能相关 ===================

    public String getGroupMessage(Long groupId) { return groupMessages.computeIfAbsent(groupId, k -> aiChatProperties.getDefaultSysMsg()); }
    public void setGroupMessage(Long groupId, String message) { groupMessages.put(groupId, message); }
    public String getUserMessage(Long userId) { return userMessages.computeIfAbsent(userId, k -> aiChatProperties.getDefaultSysMsg()); }
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
        groupMessages.remove(groupId);
        longTermGroupMemories.remove(groupId);
    }
    public void resetUser(Long userId) {
        userMessages.remove(userId);
        longTermUserMemories.remove(userId);
    }
}
