package com.zincoid.nullbot.core.component.security;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.component.tool.BotOperator;
import com.zincoid.nullbot.core.component.websocket.WebSocketSender;
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
public class SecurityCodeScheduler {

    private final BotOperator botOperator;  // 管理群通知工具
    private final WebSocketSender webSocketSender;  // 客户端通知工具
    private final ScheduledExecutorService scheduler;  // 任务调度器
    private final ConcurrentHashMap<String, CodeEntry> codeEntries;  // 存储安全码及调度任务

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");
    private static final long DEFAULT_REFRESH_INTERVAL = 600_000;  // 默认刷新间隔: 10 Min
    // private static final long DEFAULT_REFRESH_INTERVAL = 10_000;  // 测试刷新间隔: 10 Sec

    @AllArgsConstructor
    private static class CodeEntry {
        private final String code;
        private final ScheduledFuture<?> future;
        private final long refreshInterval;
        private final boolean logging;
    }

    public SecurityCodeScheduler(BotOperator botOperator, WebSocketSender webSocketSender) {
        this.botOperator = botOperator;
        this.webSocketSender = webSocketSender;
        scheduler = Executors.newScheduledThreadPool(5);
        codeEntries = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {  // 初始化安全码 (废弃 无法发送群日志)
        // createCode("regist");
        // createCode("access", 86_400_000, true);
        log.info("▽ [SecurityCodeScheduler] 安全码调度器已初始化");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initCode() {  // 初始化安全码
        createCode("regist");
        createCode("access", 86_400_000, false);
        log.info("▽ [SecurityCodeScheduler] 默认安全码已初始化");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("▽ [SecurityCodeScheduler] 安全码调度器已关闭");
    }

    // =================== 调用方法 ===================

    /**
     * 创建安全码 (使用默认刷新间隔 无群日志)
     * @param codeId 安全码标识
     * @return 初始安全码
     */
    public String createCode(String codeId) {
        return createCode(codeId, -1, false);
    }

    /**
     * 创建安全码
     * @param codeId 安全码标识
     * @param refreshInterval 刷新间隔 (ms) 非正则使用默认值
     * @param logging 是否启用群日志
     * @return 初始安全码
     */
    public String createCode(String codeId, long refreshInterval, boolean logging) {
        if (!StringUtils.hasLength(codeId)) throw new IllegalArgumentException("码标识不合法");
        if (codeEntries.containsKey(codeId)) removeCode(codeId);  // 移除原有任务
        String initCode = UUID.randomUUID().toString();
        refreshInterval = refreshInterval > 0 ? refreshInterval : DEFAULT_REFRESH_INTERVAL;
        ScheduledFuture<?> future = scheduler.schedule(  // 创建调度任务
                () -> refreshCode(codeId, true),
                refreshInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(initCode, future, refreshInterval, logging));  // 存储
        log.info("▽ [SecurityCodeScheduler] 安全码已创建 - CodeId: {}, InitCode: {}", codeId, initCode);
        if (logging) botOperator.sendLogGroupMsg("""
                [安全码调度] 🔑已初始化
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
    public void removeCode(String codeId) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.remove(codeId);
        if (entry.future != null)
            entry.future.cancel(false);
        log.info("▽ [SecurityCodeScheduler] 安全码已移除 - CodeId: {}", codeId);
    }

    /**
     * 刷新安全码
     * @param codeId 安全码标识
     * @param resetTimer 是否重置刷新计时
     * @return 新的安全码值
     */
    public String refreshCode(String codeId, boolean resetTimer) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.get(codeId);
        if (resetTimer && entry.future != null)
            entry.future.cancel(false);  // 取消当前调度
        String newCode = UUID.randomUUID().toString();  // 生成新安全码
        codeEntries.put(codeId, new CodeEntry(  // 更新
                newCode,
                resetTimer ? scheduler.schedule(  // 重新调度
                        () -> refreshCode(codeId, true),
                        entry.refreshInterval,
                        TimeUnit.MILLISECONDS
                ) : entry.future,
                entry.refreshInterval,
                entry.logging
        ));
        log.info("▽ [SecurityCodeScheduler] 安全码已刷新 - CodeId: {}, NewCode: {}", codeId, newCode);
        if (entry.logging) {
            webSocketSender.broadcast("INFO", "安全码已刷新 -> %s: %s".formatted(codeId, newCode));
            botOperator.sendLogGroupMsg("""
                [安全码调度] 🔑已刷新
                - CodeID: %s
                - NextOn: %s
                - NewCode: %s"""
                    .formatted(codeId, resetTimer ? LocalDateTime.now().plus(Duration.ofMillis(entry.refreshInterval)).format(formatter) : "Original", newCode)
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
    public boolean validateCode(String codeId, String codeToCheck) {
        codeExistenceValidation(codeId);
        return codeEntries.get(codeId).code.equals(codeToCheck);
    }

    // =================== 辅助方法 ===================

    /**
     * 更新安全码刷新间隔
     * @param codeId 安全码标识
     * @param newInterval 新的刷新间隔 (ms) 非正则使用默认值
     */
    public void updateInterval(String codeId, long newInterval) {
        codeExistenceValidation(codeId);
        newInterval = newInterval > 0 ? newInterval : DEFAULT_REFRESH_INTERVAL;
        CodeEntry entry = codeEntries.get(codeId);
        if (entry.future != null) entry.future.cancel(false);  // 取消当前调度
        ScheduledFuture<?> newFuture = scheduler.schedule(  // 重新调度
                () -> refreshCode(codeId, true),
                newInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(entry.code, newFuture, newInterval, entry.logging));  // 更新
        log.info("▽ [SecurityCodeScheduler] 安全码刷新间隔已更新 - CodeId: {}, NewInterval: {} ms", codeId, newInterval);
    }

    /**
     * 获取安全码
     * @param codeId 安全码标识
     * @return 安全码值
     */
    public String getCode(String codeId) {
        codeExistenceValidation(codeId);
        return codeEntries.get(codeId).code;
    }

    /**
     * 获取安全码 (全部)
     * @return 安全码映射表
     */
    public Map<String, String> getCodes() {
        Map<String, String> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, CodeEntry> entry : codeEntries.entrySet())
            result.put(entry.getKey(), entry.getValue().code);
        return result;
    }

    // =================== 工具方法 ===================

    private void codeExistenceValidation(String codeId) {
        if (!codeEntries.containsKey(codeId))
            throw new IllegalArgumentException("安全码不存在");
    }
}
