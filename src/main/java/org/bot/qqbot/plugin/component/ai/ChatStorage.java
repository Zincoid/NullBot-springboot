package org.bot.qqbot.plugin.component.ai;

import lombok.Data;
import org.bot.qqbot.entity.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class ChatStorage
{
    private final Map<Long, List<ChatMessage>> userHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> groupHistories = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatMessage>> monitorHistories = new ConcurrentHashMap<>();

    public List<ChatMessage> getUserHistory(Long userId) { return userHistories.computeIfAbsent(userId, k -> new ArrayList<>()); }

    public List<ChatMessage> getGroupHistory(Long groupId) { return groupHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }

    public List<ChatMessage> getMonitorHistory(Long groupId) { return monitorHistories.computeIfAbsent(groupId, k -> new ArrayList<>()); }

    public void trimHistory(List<ChatMessage> history, int maxHistoryLength) {
        if (history.size() > maxHistoryLength) {
            int removeCount = history.size() - maxHistoryLength;
            int startIndex = 0;
            if ("system".equals(history.get(0).getRole())) startIndex = 1;  // 跳过系统消息
            for (int i = 0; i < removeCount; i++) history.remove(startIndex);  // 移除最旧消息对
        }
    }

    public String getUserHistoryAsString(Long userId) { return getString(userId, userHistories); }

    public String getGroupHistoryAsString(Long groupId) { return getString(groupId, groupHistories); }

    public String getMonitorHistoryAsString(Long groupId) { return getString(groupId, monitorHistories); }

    private String getString(Long id, Map<Long, List<ChatMessage>> histories) {
        StringBuilder sb = new StringBuilder();
        List<ChatMessage> history =  histories.get(id);
        if (history == null || history.isEmpty()) return "无对话历史";
        for (ChatMessage msg : history) {
            if("user".equals(msg.getRole()))
                sb.append(msg.getUserName()).append("(").append(msg.getUserId()).append("): ").append(msg.getContent()).append("\n---\n");
            else
                sb.append("AI: ").append(msg.getContent()).append("\n---\n");
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
}
