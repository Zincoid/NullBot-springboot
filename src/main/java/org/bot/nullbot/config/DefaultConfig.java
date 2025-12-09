package org.bot.nullbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot")
public class DefaultConfig {
    private Boolean enableImageCollect;
    private Boolean enableKeywordDetect;
    private Boolean enablePokeDetect;
    private Boolean enableMessageCollect;
    private Boolean enableRecallDetect;
    private Map<Long, Integer> accesses;
}
