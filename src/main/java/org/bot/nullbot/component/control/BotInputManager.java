package org.bot.nullbot.component.control;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.entity.BotInputer;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
public class BotInputManager {

    private static final Map<String, InputEntry> inputEntries = new ConcurrentHashMap<>();
    private static final Map<String, List<Pair<Long, String>>> inputCaches = new ConcurrentHashMap<>();

    @AllArgsConstructor
    private static class InputEntry {
        private final CompletableFuture<List<Pair<Long, String>>> future;
        private final Pattern pattern;
        private boolean coverable;
    }

    // ============= BotInputer 注册方法 ==============

    public static void register(BotInputer inputer) {
        request(inputer.getMode(), inputer.getTargetId(),
                inputer.getPattern(), inputer.getTimeout(), inputer.isCoverable());
    }

    // =================== 调用方法 ===================

    /* 注册输入事件 (默认非可覆盖模式) - 阻塞直到收到响应或超时 (视模式而定) */
    public static List<Pair<Long, String>> request(BniMode mode, Long targetId, String pattern, long timeout) {
        return request(mode, targetId, pattern, timeout, false);
    }

    /* 注册输入事件 - 阻塞直到收到响应或超时 (视模式而定) */
    public static List<Pair<Long, String>> request(BniMode mode, Long targetId, String pattern, long timeout, boolean coverable) {
        String id = switch (mode) {
            case PS -> "PS_%s".formatted(targetId);  // 个人单值模式 targetId为用户ID 超时返回空列表
            case GS -> "GS_%s".formatted(targetId);  // 群组单值模式 targetId为群聊ID 超时返回空列表
            case GM -> "GM_%s".formatted(targetId);  // 群组多值模式 targetId为群聊ID 超时返回已输入值列表
        };
        if (inputEntries.containsKey(id)) {
            if (inputEntries.get(id).coverable) {
                cancelWait(mode, targetId);
                log.info("▽ [BotInputManager] 已取消 {} 可覆盖的冲突输入 (Mode: {})", targetId, mode);
            } else throw new NullBotMsgException("[输入] ❌事件冲突");
        }
        if (mode == BniMode.GM)
            inputCaches.put(id, Collections.synchronizedList(new ArrayList<>()));
        CompletableFuture<List<Pair<Long, String>>> future = new CompletableFuture<>();
        inputEntries.put(id, new InputEntry(future, Pattern.compile(pattern), coverable));
        try {
            log.info("▽ [BotInputManager] 等待 {} 输入 (Mode: {}, Timeout: {} Sec)", targetId, mode, timeout);
            return future.orTimeout(timeout, TimeUnit.SECONDS)
                    .exceptionally(e -> {
                        log.info("▽ [BotInputManager] {} 输入超时 (Mode: {})", targetId, mode);
                        return mode == BniMode.GM ? inputCaches.remove(id) : new ArrayList<>();
                    })
                    .get();
        } catch (Exception e) {
            log.error("▽ [BotInputManager] 输入事件异常 (Mode: {})", mode, e);
            throw new NullBotMsgException("[输入] ❌事件异常");
        } finally {
            inputEntries.remove(id);
        }
    }

    /* 响应输入事件 -  自动匹配所有模式输入事件 */
    public static boolean response(Long groupId, Long userId, String message) {
        boolean hasResponse = false;
        if (inputEntries.containsKey("PS_%s".formatted(userId)))
            if (_response(BniMode.PS, groupId, userId, message)) hasResponse = true;
        if (inputEntries.containsKey("GS_%s".formatted(groupId)))
            if (_response(BniMode.GS, groupId, userId, message)) hasResponse = true;
        if (inputEntries.containsKey("GM_%s".formatted(groupId)))
            if (_response(BniMode.GM, groupId, userId, message)) hasResponse = true;
        return hasResponse;
    }

    /* 响应输入事件 (按模式) - 按模式匹配输入事件 */
    private static boolean _response(BniMode mode, Long groupId, Long userId, String message) {
        String id = switch (mode) {
            case PS -> "PS_%s".formatted(userId);
            case GS -> "GS_%s".formatted(groupId);
            case GM -> "GM_%s".formatted(groupId);
        };
        InputEntry entry = inputEntries.get(id);
        if (entry == null || entry.future == null || entry.future.isDone()) return false;
        if (!entry.pattern.matcher(message).matches()) return false;
        if (mode == BniMode.GM)
            inputCaches.get(id).add(Pair.of(userId, message));
        else
            entry.future.complete(Collections.singletonList(Pair.of(userId, message)));
        log.info("▽ [BotInputManager] 群聊 {} 用户 {} 已响应 (Mode: {}) - {}", groupId, userId, mode, message);
        return true;
    }

    // =================== 工具方法 ===================

    public static boolean isWaiting(BniMode mode, Long targetId) {
        String id = switch (mode) {
            case PS -> "PS_%s".formatted(targetId);
            case GS -> "GS_%s".formatted(targetId);
            case GM -> "GM_%s".formatted(targetId);
        };
        InputEntry entry = inputEntries.get(id);
        return entry != null && entry.future != null && !entry.future.isDone();
    }

    public static boolean cancelWait(BniMode mode, Long targetId) {
        String id = switch (mode) {
            case PS -> "PS_%s".formatted(targetId);
            case GS -> "GS_%s".formatted(targetId);
            case GM -> "GM_%s".formatted(targetId);
        };
        InputEntry entry = inputEntries.remove(id);
        if (entry == null || entry.future == null || entry.future.isDone()) return false;
        entry.future.complete(new ArrayList<>());
        return true;
    }
}
