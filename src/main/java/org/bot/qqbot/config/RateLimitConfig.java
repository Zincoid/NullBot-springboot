package org.bot.qqbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.command.rate-limit")
public class RateLimitConfig {
    private Boolean enabled;
    private Integer capacity;
    private Integer refill;
    private Scope scope;

    public enum Scope { User, Group, Command, Global }
}
