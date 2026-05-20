package com.zincoid.nullbot.core.config.prop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.command.rate-limit")
public class RateLimitProperties {
    private Boolean enabled;
}
