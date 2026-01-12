package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.entity.ChatMessage;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.enums.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Component
public class ChatStorage
{
    private final Map<Long, ReentrantLock> groupLocks = new ConcurrentHashMap<>();
    private final Map<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private final Map<Long, List<ChatMessage>> userHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> groupHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> monitorHistories = new ConcurrentHashMap<>();

    private final Map<Long, LocalDateTime> banMap = new ConcurrentHashMap<>();

    private final List<String> errorMessages = new CopyOnWriteArrayList<>();

    // =================== 并发功能相关 ===================

    public ReentrantLock getGroupLock(Long groupId) {
        return groupLocks.computeIfAbsent(groupId, k -> new ReentrantLock(true)); // 群组公平锁
    }

    public ReentrantLock getUserLock(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock(true)); // 用户公平锁
    }

    // =================== 封禁功能相关 ===================

    public void banUser(Long userId, int time) { banMap.put(userId, LocalDateTime.now().plusMinutes(time)); }

    public boolean isUserBanned(Long userId) {
        LocalDateTime banUntil = banMap.get(userId);
        if (banUntil == null) return false; // 用户未被封禁
        if (LocalDateTime.now().isAfter(banUntil)) {
            banMap.remove(userId);  // 封禁时间已过 自动清理
            return false;
        }
        return true;
    }

    public LocalDateTime getUserBannedUntil(Long userId) { return banMap.get(userId); }

    // =================== 撤回功能相关 ===================

    public List<ChatMessage> getAIMessagesForRecall(ChatOption option, Long groupId, Long userId, int n) {
        ReentrantLock lock = switch (option.getScope()) {
            case Group, Monitor -> getGroupLock(groupId);
            case Personal -> getUserLock(userId);
        };
        lock.lock();  // 锁定历史存储

        try {
            List<ChatMessage> history = switch (option.getScope()) {
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

    // =================== 历史功能相关 ===================

    public List<ChatMessage> getUserHistory(Long userId) { return userHistories.computeIfAbsent(userId, k -> new ArrayList<>()); }
    public List<ChatMessage> getGroupHistory(Long groupId) { return groupHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }
    public List<ChatMessage> getMonitorHistory(Long groupId) { return monitorHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }

    public void trimHistory(List<ChatMessage> history, int maxHistoryLength) {
        if (history.size() > maxHistoryLength) {
            int removeCount = history.size() - maxHistoryLength;
            int startIndex = 0;
            if ("system".equals(history.getFirst().getRole())) startIndex = 1;  // 跳过系统消息
            for (int i = 0; i < removeCount; i++) history.remove(startIndex);  // 移除最旧消息
        }
    }

    public String getUserHistoryAsString(Long userId, ChatOption option) { return getHistoryStringForAI(userId, userHistories, option); }
    public String getGroupHistoryAsString(Long groupId, ChatOption option) { return getHistoryStringForAI(groupId, groupHistories, option); }
    public String getMonitorHistoryAsString(Long groupId, ChatOption option) { return getHistoryStringForAI(groupId, monitorHistories, option); }

    private String getHistoryStringForAI(Long id, Map<Long, List<ChatMessage>> histories, ChatOption option) {
        StringBuilder sb = new StringBuilder();
        List<ChatMessage> history =  histories.get(id);
        if (history == null || history.isEmpty()) return "\n无对话历史";
        for (ChatMessage msg : history) {
            if("user".equals(msg.getRole()))
                sb.append("\n---\n").append(msg.getUserName()).append("(").append(msg.getUserId()).append("): ").append(msg.getContent());
            else{
                String content = msg.getContent();
                if(!option.isCustom() && option.isEmbedding())
                    if(content.startsWith("{") && content.endsWith("}")) continue;
                sb.append("\n---\n").append("AI: ").append(content);
            }

        }
        return sb.toString();
    }

    public void clearUserHistory(Long userId) { userHistories.remove(userId); }
    public void clearGroupHistory(Long groupId) { groupHistories.remove(groupId); }
    public void clearMonitorHistory(Long groupId) { monitorHistories.remove(groupId); }

    public void resetAllHistories() {
        userHistories.clear();
        groupHistories.clear();
        monitorHistories.clear();
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
