package com.zincoid.nullbot.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.chat.openai")
public class OpenAiProperties {
    private String apiKey;
    private String apiUrl;
    private String model;
}
