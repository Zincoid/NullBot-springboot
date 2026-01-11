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
    private boolean imageCollect;
    private boolean keywordDetect;
    private boolean pokeDetect;
    private boolean messageCollect;
    private boolean recallDetect;
    private double guessRatio;
    private int guessPadding;
}
