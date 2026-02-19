package org.bot.nullbot.component.control;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class BotNextInputer
{
    private final Map<Long, InputEntry> inputEntries = new ConcurrentHashMap<>();

    @AllArgsConstructor
    private static class InputEntry {
        private final Pattern pattern;
        private final CompletableFuture<String> future;
    }

    // =================== 调用方法 ===================

    /* 注册输入事件 - 阻塞直到收到响应或超时 */
    public String request(Long userId, long timeout, String pattern) {
        CompletableFuture<String> future = new CompletableFuture<>();
        inputEntries.put(userId, new InputEntry(Pattern.compile(pattern), future));
        try {
            log.info("▽ [BotNextInputer] 等待用户 {} 输入 (Timeout: {} Sec)", userId, timeout);
            return future.orTimeout(timeout, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        log.info("▽ [BotNextInputer] 用户 {} 输入超时", userId);
                        return null;
                    })
                    .get();
        } catch (Exception e) {
            log.error("▽ [BotNextInputer] 输入事件异常", e);
            return null;
        } finally {
            inputEntries.remove(userId);
        }
    }

    /* 响应输入事件 */
    public boolean response(Long userId, String message) {
        InputEntry entry = inputEntries.remove(userId);
        if (entry != null && !entry.future.isDone()) {
            if (!entry.pattern.matcher(message).matches()) {
                inputEntries.put(userId, entry);
                return false;
            }
            entry.future.complete(message);
            log.info("▽ [BotNextInputer] 用户 {} 已响应 - {}", userId, message);
            return true;
        }
        return false;
    }

    // =================== 工具方法 ===================

    public boolean isWaiting(Long userId) {
        CompletableFuture<String> future = inputEntries.get(userId).future;
        return future != null && !future.isDone();
    }

    public boolean cancelWait(Long userId) {
        CompletableFuture<String> future = inputEntries.remove(userId).future;
        if (future != null && !future.isDone()) {
            future.complete(null);
            return true;
        }
        return false;
    }
}
