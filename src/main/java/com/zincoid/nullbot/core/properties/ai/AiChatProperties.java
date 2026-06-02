package com.zincoid.nullbot.core.properties.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat")
public class AiChatProperties {
    private Integer maxHistoryLength;
    private Integer maxTokens;
    private Integer maxToolCalls;
    private String DefaultSysMsg;
}
