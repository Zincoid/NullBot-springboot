package com.zincoid.nullbot.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.tts")
public class TtsProperties {
    private String apiUrl;
    private String apiKey;
    private String version;
    private String modelName;
    private String promptTextLang;
    private String textLang;
    private String emotion;
}
