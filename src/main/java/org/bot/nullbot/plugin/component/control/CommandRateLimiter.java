package org.bot.nullbot.plugin.component.control;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import io.github.bucket4j.*;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.RateLimitConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandRateLimiter
{
    private final RateLimitConfig rateLimitConfig;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(rateLimitConfig.getCapacity()).refillGreedy(rateLimitConfig.getRefill(), Duration.ofMinutes(1)))
                .build());
    }

    public boolean tryConsume(CommandEvent<?> commandEvent) {
        if (!rateLimitConfig.getEnabled()){
            return true;
        }
        if(commandEvent.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String key = switch (rateLimitConfig.getScope()) {
                case User -> "user:" + groupMessageEvent.getSender().getUserId();
                case Group -> "group:" + groupMessageEvent.getGroupId();
                case Command -> "cmd:" + commandEvent.getCommandType();
                case Global -> "global";
            };
            Bucket bucket = resolveBucket(key);
            return bucket.tryConsume(1);
        }else if(commandEvent.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent){
            // 暂不限制
            return true;
        }else
            return true;
    }
}
