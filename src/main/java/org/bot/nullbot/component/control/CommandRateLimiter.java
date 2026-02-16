package org.bot.nullbot.component.control;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.prop.RateLimitProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandRateLimiter
{
    private final RateLimitProperties rateLimitProperties;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastProcess = new ConcurrentHashMap<>();

    // =================== 调用方法 ===================

    public boolean tryConsume(Long groupId, Long userId, String commandType) {
        if (!rateLimitProperties.getEnabled()) {
            return true;
        }
        if (isSpam(groupId, 500)) {
            return false;
        }
        String key = switch (rateLimitProperties.getScope()) {
            case User -> "user:" + userId;
            case Group -> "group:" + groupId;
            case Command -> "cmd:" + commandType;
            case Global -> "global";
        };
        return resolveBucket(key).tryConsume(1);
    }

    // =================== 工具方法 ===================

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(rateLimitProperties.getCapacity())
                        .refillGreedy(rateLimitProperties.getRefill(), Duration.ofMinutes(1)))
                .build());
    }

    public boolean isSpam(Long groupId, long msLimit) {
        long now = System.currentTimeMillis();
        long last = lastProcess.getOrDefault(groupId, 0L);
        if (now - last < msLimit) return true;
        lastProcess.put(groupId, now);
        return false;
    }
}
