package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.config.prop.DeepSeekProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class SysMsgStorage
{
    private final DeepSeekProperties deepSeekProperties;

    private final Map<Long, String> defaultMessages = new ConcurrentHashMap<>();
    private final Map<Long, String> customMessages = new ConcurrentHashMap<>();

    public String getDefaultMessage(Long groupId) { return defaultMessages.computeIfAbsent(groupId, k -> deepSeekProperties.getDefaultSystemMessage()); }
    public void setDefaultMessage(Long groupId, String message) { defaultMessages.put(groupId, message); }

    public String getCustomMessage(Long groupId) { return customMessages.computeIfAbsent(groupId, k -> "你是一个AI助手，名字叫Null。"); }
    public void setCustomMessage(Long groupId, String message) { customMessages.put(groupId, message); }

    public void reset(Long groupId) {
        defaultMessages.remove(groupId);
        customMessages.remove(groupId);
    }
}
