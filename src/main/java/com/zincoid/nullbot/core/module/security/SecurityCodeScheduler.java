package com.zincoid.nullbot.core.module.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.module.system.WsSender;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityCodeScheduler {

    private static final long DEFAULT_REFRESH_INTERVAL = 600_000;  // 默认刷新间隔: 10 Min
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    private final BotOperator botOperator;
    private final WsSender wsSender;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final ConcurrentHashMap<String, CodeEntry> codeEntries = new ConcurrentHashMap<>();

    private record CodeEntry(String code, ScheduledFuture<?> future, long refreshInterval, boolean logging) {}

    @PostConstruct
    public void init() {
        // create("regist");  // 初始化安全码 (废弃 无法发送群日志)
        // create("access", 86_400_000, true);
        log.info("▽ [SecurityCodeScheduler] 安全码调度器已启动");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initCode() {
        create("regist");
        create("access", 86_400_000, false);
        log.info("▽ [SecurityCodeScheduler] 默认安全码已初始化");
    }

    @PreDestroy
    public void destroy() {
        if (!scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("▽ [SecurityCodeScheduler] 安全码调度器已关闭");
    }

    // =================== 调用方法 ===================

    /**
     * 创建安全码 (使用默认刷新间隔 无群日志)
     * @param codeId 安全码标识
     * @return 初始安全码
     */
    public String create(String codeId) {
        return create(codeId, -1, false);
    }

    /**
     * 创建安全码
     * @param codeId 安全码标识
     * @param refreshInterval 刷新间隔 (ms) 非正则使用默认值
     * @param logging 是否启用群日志
     * @return 初始安全码
     */
    public String create(String codeId, long refreshInterval, boolean logging) {
        if (!StringUtils.hasLength(codeId)) throw new IllegalArgumentException("码标识不合法");
        if (codeEntries.containsKey(codeId)) remove(codeId);  // 移除原有任务
        String initCode = UUID.randomUUID().toString();
        refreshInterval = refreshInterval > 0 ? refreshInterval : DEFAULT_REFRESH_INTERVAL;
        ScheduledFuture<?> future = scheduler.schedule(  // 创建调度任务
                () -> refresh(codeId, true),
                refreshInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(initCode, future, refreshInterval, logging));  // 存储
        log.info("▽ [SecurityCodeScheduler] 安全码已创建 - CodeID: {}, InitCode: {}", codeId, initCode);
        if (logging) botOperator.sendLogGroupMsg("""
                🔑安全码已初始化
                - CodeID: %s
                - Interval: %sms
                - InitCode: %s"""
                .formatted(codeId, refreshInterval, initCode)
        );
        return initCode;
    }

    /**
     * 移除安全码
     * @param codeId 安全码标识
     */
    public void remove(String codeId) {
        checkExistence(codeId);
        CodeEntry entry = codeEntries.remove(codeId);
        if (entry.future != null)
            entry.future.cancel(false);
        log.info("▽ [SecurityCodeScheduler] 安全码已移除 - CodeID: {}", codeId);
    }

    /**
     * 刷新安全码
     * @param codeId 安全码标识
     * @param resetTimer 是否重置刷新计时
     * @return 新的安全码值
     */
    public String refresh(String codeId, boolean resetTimer) {
        checkExistence(codeId);
        CodeEntry entry = codeEntries.get(codeId);
        if (resetTimer && entry.future != null)
            entry.future.cancel(false);  // 取消当前调度
        String newCode = UUID.randomUUID().toString();  // 生成新安全码
        codeEntries.put(codeId, new CodeEntry(  // 更新
                newCode,
                resetTimer ? scheduler.schedule(  // 重新调度
                        () -> refresh(codeId, true),
                        entry.refreshInterval,
                        TimeUnit.MILLISECONDS
                ) : entry.future,
                entry.refreshInterval,
                entry.logging
        ));
        log.info("▽ [SecurityCodeScheduler] 安全码已刷新 - CodeID: {}, NewCode: {}", codeId, newCode);
        if (entry.logging) {
            wsSender.broadcast("INFO", "安全码已刷新 -> %s: %s".formatted(codeId, newCode));
            botOperator.sendLogGroupMsg("""
                🔑安全码已刷新
                - CodeID: %s
                - NextOn: %s
                - NewCode: %s"""
                    .formatted(codeId, resetTimer ? LocalDateTime.now().plus(Duration.ofMillis(entry.refreshInterval)).format(FORMATTER) : "Original", newCode)
            );
        }
        return newCode;
    }

    /**
     * 验证安全码
     * @param codeId 安全码标识
     * @param codeToCheck 待检查安全码值
     * @return 是否有效
     */
    public boolean validate(String codeId, String codeToCheck) {
        checkExistence(codeId);
        return codeEntries.get(codeId).code.equals(codeToCheck);
    }

    // =================== 辅助方法 ===================

    /**
     * 更新安全码刷新间隔
     * @param codeId 安全码标识
     * @param newInterval 新的刷新间隔 (ms) 非正则使用默认值
     */
    public void updateInterval(String codeId, long newInterval) {
        checkExistence(codeId);
        newInterval = newInterval > 0 ? newInterval : DEFAULT_REFRESH_INTERVAL;
        CodeEntry entry = codeEntries.get(codeId);
        if (entry.future != null) entry.future.cancel(false);  // 取消当前调度
        ScheduledFuture<?> newFuture = scheduler.schedule(  // 重新调度
                () -> refresh(codeId, true),
                newInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(entry.code, newFuture, newInterval, entry.logging));  // 更新
        log.info("▽ [SecurityCodeScheduler] 安全码刷新间隔已更新 - CodeID: {}, NewInterval: {} ms", codeId, newInterval);
    }

    /**
     * 获取安全码
     * @param codeId 安全码标识
     * @return 安全码值
     */
    public String get(String codeId) {
        checkExistence(codeId);
        return codeEntries.get(codeId).code;
    }

    /**
     * 获取安全码 (全部)
     * @return 安全码映射表
     */
    public Map<String, String> getAll() {
        Map<String, String> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, CodeEntry> entry : codeEntries.entrySet())
            result.put(entry.getKey(), entry.getValue().code);
        return result;
    }

    // =================== 工具方法 ===================

    private void checkExistence(String codeId) {
        if (!codeEntries.containsKey(codeId))
            throw new IllegalArgumentException("安全码不存在");
    }
}
