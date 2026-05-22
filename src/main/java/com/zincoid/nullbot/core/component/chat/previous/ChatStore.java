package com.zincoid.nullbot.core.component.chat.previous;

import lombok.Data;
import com.zincoid.nullbot.core.model.message.ChatMessage;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.service.SettingService;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Data
public class ChatStore {

    private final Map<Long, List<ChatMessage>> userHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> groupHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> monitorHistories = new ConcurrentHashMap<>();

    private final Map<Long, ReentrantLock> groupLocks = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private final List<String> errorMessages = new CopyOnWriteArrayList<>();

    private final SettingService settingService;

    // =================== 历史功能相关 ===================

    public List<ChatMessage> getUserHistory(Long userId) { return userHistories.computeIfAbsent(userId, k -> new ArrayList<>()); }
    public List<ChatMessage> getGroupHistory(Long groupId) { return groupHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }
    public List<ChatMessage> getMonitorHistory(Long groupId) { return monitorHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }

    public void trimHistory(List<ChatMessage> history, int maxHistoryLength) {
        if (history.size() > maxHistoryLength) {
            int removeCount = history.size() - maxHistoryLength;
            int startIndex = 0;
            if ("system".equals(history.getFirst().getRole())) startIndex = 1;  // 跳过系统消息
            history.subList(startIndex, startIndex + removeCount).clear();  // 移除最旧消息
        }
    }

    public void clearUserHistory(Long userId) { userHistories.remove(userId); }
    public void clearGroupHistory(Long groupId) { groupHistories.remove(groupId); }
    public void clearMonitorHistory(Long groupId) { monitorHistories.remove(groupId); }

    public void resetAllHistories() {
        userHistories.clear();
        groupHistories.clear();
        monitorHistories.clear();
    }

    // =================== 并发功能相关 ===================

    public ReentrantLock getGroupLock(Long groupId) {
        return groupLocks.computeIfAbsent(groupId, k -> new ReentrantLock(true)); // 群组公平锁
    }

    public ReentrantLock getUserLock(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock(true)); // 用户公平锁
    }

    // =================== 撤回功能相关 ===================

    public List<ChatMessage> getAIMessagesForRecall(Long groupId, Long userId, int n) {
        SettingPO setting = BotCtxUtil.getSetting();
        ReentrantLock lock = switch (setting.getChatScope()) {
            case Group, Monitor -> getGroupLock(groupId);
            case Personal -> getUserLock(userId);
        };
        lock.lock();  // 锁定历史存储

        try {
            List<ChatMessage> history = switch (setting.getChatScope()) {
                case Group -> groupHistories.get(groupId);
                case Monitor -> monitorHistories.get(groupId);
                case Personal -> userHistories.get(userId);
            };
            if (history == null || history.isEmpty()) return new ArrayList<>();

            List<ChatMessage> filtered = history.stream()
                    .filter(msg -> msg != null && msg.getMessageId() != null && "assistant".equals(msg.getRole()))
                    .toList();

            int startIndex = Math.max(0, filtered.size() - n);
            return filtered.subList(startIndex, filtered.size());
        } finally {
            lock.unlock();  // 解锁历史存储
        }
    }

    // =================== 纠错功能相关 ===================

    public void recordError(String error) {
        if (errorMessages.size() >= 50) errorMessages.removeFirst();
        errorMessages.add(error);
    }

    public String getErrors() {
        if (errorMessages.isEmpty()) return "无Error记录";
        return String.join("\n", errorMessages);
    }
}
