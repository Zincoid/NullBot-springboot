package org.bot.qqbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot")
public class FunctionDefaultConfig {
    private Boolean enableImageCollect;
    private Boolean enableKeywordDetect;
    private Map<Long, Integer> accesses;
}
