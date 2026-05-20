package org.bot.nullbot.component.control;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.setting.LimitOption;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandRateLimiter {

    private final SettingService settingService;

    @Getter  // 调试用
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastProcess = new ConcurrentHashMap<>();

    // =================== 调用方法 ===================

    public boolean tryConsume(Long groupId, Long userId, String commandType) {
        if (isSpam(groupId, 500)) return false;
        LimitOption option = settingService.getLimitOption(groupId);
        String key = switch (option.getLimitScope()) {
            case Group -> "[%s]".formatted(groupId);
            case User -> "[%s][User:%s]".formatted(groupId, userId);
            case Cmd -> "[%s][Cmd:%s]".formatted(groupId, commandType);
        };
        return resolveBucket(key, option).tryConsume(1);
    }

    public void reset(Long groupId) {
        for (String key : buckets.keySet()) {
            if (key.startsWith("[%s]".formatted(groupId))) {
                buckets.remove(key);
            }
        }
    }

    // =================== 工具方法 ===================

    public boolean isSpam(Long groupId, long msLimit) {
        long now = System.currentTimeMillis();
        long last = lastProcess.getOrDefault(groupId, 0L);
        if (now - last < msLimit) return true;
        lastProcess.put(groupId, now);
        return false;
    }

    private Bucket resolveBucket(String key, LimitOption option) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(option.getLimitCapacity())
                        .refillGreedy(option.getLimitRefill(),
                                Duration.ofMinutes(option.getLimitInterval())))
                .build());
    }
}
