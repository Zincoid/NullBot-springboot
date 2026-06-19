package com.zincoid.nullbot.core.properties.bot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bot.command")
public class CmdProperties {

    private String prefix;
    private boolean ignoreCase;
    private boolean limit;
}
