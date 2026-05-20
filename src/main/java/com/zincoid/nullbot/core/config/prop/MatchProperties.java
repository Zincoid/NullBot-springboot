package com.zincoid.nullbot.core.config.prop;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class MatchProperties {
    // 等待匹配超时（秒）
    private final long waitingTimeoutSeconds = 120;
    // 对局无操作超时（秒）
    private final long playingTimeoutSeconds = 240;
}
