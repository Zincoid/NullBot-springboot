package org.bot.nullbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot.default")
public class DefaultConfig
{
    private Boolean imageCollect;
    private Boolean keywordDetect;
    private Boolean pokeDetect;
    private Boolean messageCollect;
    private Boolean recallDetect;
    private Double guessRatio;
    private Integer guessPadding;
}
