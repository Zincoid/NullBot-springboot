package com.zincoid.nullbot.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties {
    private Integer maxHistoryLength;
    private Integer maxTokens;
    private String DefaultSystemMessage;
}
