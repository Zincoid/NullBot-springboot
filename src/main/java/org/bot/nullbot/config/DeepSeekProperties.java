package org.bot.nullbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.ai.deepseek")
public class DeepSeekProperties
{
    private String apiKey;
    private String apiUrl;
    private Integer maxHistoryLength;
    private Integer maxMonitorLength;
    private Integer maxTokens;
    private String MemePath;
    private String DefaultSystemMessage;
}
