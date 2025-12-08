package org.bot.nullbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class DeepSeekConfig {
    private String apiKey;
    private String apiUrl;
    private Integer maxHistoryLength;
    private Integer maxMonitorLength;
    private Integer maxTokens;
    private SystemMessageConfig systemMessage;

    @Data
    public static class SystemMessageConfig {
        private String group;
        private String personal;
        private String monitor;
    }
}
