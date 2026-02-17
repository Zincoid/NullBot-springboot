package org.bot.nullbot.component.control;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class BotNextInputer
{
    private final Map<Long, CompletableFuture<String>> inputFutures = new ConcurrentHashMap<>();

    // =================== 调用方法 ===================

    /* 注册输入事件 - 阻塞直到收到响应或超时 */
    public String request(Long userId, long timeout) {
        CompletableFuture<String> future = new CompletableFuture<>();
        inputFutures.put(userId, future);
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
            inputFutures.remove(userId);
        }
    }

    /* 响应输入事件 */
    public boolean response(Long userId, String message) {
        CompletableFuture<String> future = inputFutures.remove(userId);
        if (future != null && !future.isDone()) {
            future.complete(message);
            log.info("▽ [BotNextInputer] 用户 {} 已响应 - {}", userId, message);
            return true;
        }
        log.info("▽ [BotNextInputer] 用户 {} 未等待输入", userId);
        return false;
    }

    // =================== 工具方法 ===================

    public boolean isWaiting(Long userId) {
        CompletableFuture<String> future = inputFutures.get(userId);
        return future != null && !future.isDone();
    }

    public boolean cancelWait(Long userId) {
        CompletableFuture<String> future = inputFutures.remove(userId);
        if (future != null && !future.isDone()) {
            future.complete(null);
            return true;
        }
        return false;
    }
}
