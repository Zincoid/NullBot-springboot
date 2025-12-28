package org.bot.nullbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "nullbot")
public class DefaultConfig
{
    private Boolean imageCollect;
    private Boolean keywordDetect;
    private Boolean pokeDetect;
    private Boolean messageCollect;
    private Boolean recallDetect;

    @Deprecated
    private Map<Long, Integer> userAccesses;  // 已持久化至数据库 (弃用)
    @Deprecated
    private Map<Long, Integer> groupAccesses;  // 已持久化至数据库 (弃用)
}
