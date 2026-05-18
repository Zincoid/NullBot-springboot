package org.bot.nullbot.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.ai.tts")
public class TtsProperties {
    private String apiUrl;
    private String apiKey;
    private String version;
    private String modelName;
    private String cloneGptModelName;
    private String cloneSovitsModelName;
    private String promptTextLang;
    private String textLang;
    private String emotion;
}
