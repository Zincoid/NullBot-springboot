package org.bot.nullbot.component.storage;

import lombok.Data;
import org.bot.nullbot.config.DeepSeekConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class SysMsgStorage
{
    private final Map<Long, String> customMessages;
    private final String defaultMessage;

    public SysMsgStorage(DeepSeekConfig config) {
        this.defaultMessage = config.getDefaultSystemMessage();
        customMessages = new ConcurrentHashMap<>();
    }

    public String getCustomMessage(Long groupId) {
        return customMessages.computeIfAbsent(groupId, k -> "你是一个AI助手。");
    }
}
