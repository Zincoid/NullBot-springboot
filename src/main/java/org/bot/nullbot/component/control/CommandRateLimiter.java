package org.bot.nullbot.component.control;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandRateLimiter
{
    private final SettingService settingService;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastProcess = new ConcurrentHashMap<>();

    // =================== 调用方法 ===================

    public boolean tryConsume(Long groupId, Long userId, String commandType) {
        if (isSpam(groupId, 500)) return false;
        String key = switch (settingService.getLimitScope(groupId)) {
            case User -> "User:" + userId;
            case Group -> "Group:" + groupId;
            case Command -> "Command:" + commandType;
            case Global -> "Global";
        };
        return resolveBucket(key, groupId).tryConsume(1);
    }

    // =================== 工具方法 ===================

    public boolean isSpam(Long groupId, long msLimit) {
        long now = System.currentTimeMillis();
        long last = lastProcess.getOrDefault(groupId, 0L);
        if (now - last < msLimit) return true;
        lastProcess.put(groupId, now);
        return false;
    }

    private Bucket resolveBucket(String key, Long groupId) {
    return buckets.computeIfAbsent(key, k -> Bucket.builder()
            .addLimit(limit -> limit
                    .capacity(settingService.getLimitCapacity(groupId))
                    .refillGreedy(settingService.getLimitRefill(groupId), Duration.ofMinutes(1)))
            .build());
    }
}
