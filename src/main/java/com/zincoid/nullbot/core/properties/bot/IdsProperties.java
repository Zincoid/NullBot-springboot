package com.zincoid.nullbot.core.properties.bot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "bot.ids")
public class IdsProperties {

    private Long botId;
    private Long adminId;
    private Long logId;
}
