package org.bot.nullbot.component.security;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SecurityCodeScheduler
{
    private final ScheduledExecutorService scheduler;  // 调度器
    private final ConcurrentHashMap<String, CodeEntry> codeEntries;  // 存储安全码及调度任务

    private static final long DEFAULT_REFRESH_INTERVAL = 600_000;  // 默认刷新间隔: 10 Min
    // private static final long DEFAULT_REFRESH_INTERVAL = 10_000;  // 测试刷新间隔: 10 Sec

    @AllArgsConstructor
    private static class CodeEntry {
        private final String code;
        private final ScheduledFuture<?> future;
        private final long refreshInterval;
    }

    public SecurityCodeScheduler() {
        scheduler = Executors.newScheduledThreadPool(5);
        codeEntries = new ConcurrentHashMap<>();

        // 初始化的安全码类型
        createCode("regist");
        createCode("access");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("[管理系统-安全码] 安全码调度器已关闭");
    }

    /**
     * 创建安全码 (使用默认刷新间隔)
     * @param codeId 安全码标识
     * @return 初始安全码
     */
    public String createCode(String codeId) { return createCode(codeId, null); }

    /**
     * 创建安全码
     * @param codeId 安全码标识
     * @param refreshInterval 刷新间隔 (ms) 为空则使用默认值
     * @return 初始安全码
     */
    public String createCode(String codeId, Long refreshInterval) {
        if (!StringUtils.hasLength(codeId)) throw new IllegalArgumentException("码标识不合法");
        if (codeEntries.containsKey(codeId)) removeCode(codeId);  // 停止原有任务
        String initCode = UUID.randomUUID().toString();
        long interval = (refreshInterval != null && refreshInterval > 0) ? refreshInterval : DEFAULT_REFRESH_INTERVAL;
        ScheduledFuture<?> future = scheduler.schedule(  // 创建调度任务
                () -> {
                    refreshCode(codeId);
                    scheduleNext(codeId);
                },
                interval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(initCode, future, interval));  // 存储
        log.info("[管理系统-安全码] 安全码已创建 - CodeId: {}, InitCode: {}", codeId, initCode);
        return initCode;
    }

    /**
     * 移除安全码
     * @param codeId 安全码标识
     */
    public void removeCode(String codeId) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.remove(codeId);
        if (entry.future != null) entry.future.cancel(false);
        log.info("[管理系统-安全码] 安全码已移除 - CodeId: {}", codeId);
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

    /**
     * 使用安全码 (刷新并重置刷新计时)
     * @param codeId 安全码标识
     * @return 使用的安全码值
     */
    public String useCode(String codeId) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.get(codeId);
        if (entry.future != null) entry.future.cancel(false);  // 取消当前调度
        String usedCode = entry.code;
        String newCode = UUID.randomUUID().toString();  // 生成新安全码
        ScheduledFuture<?> newFuture = scheduler.schedule(  // 重新调度
                () -> {
                    refreshCode(codeId);
                    scheduleNext(codeId);
                },
                entry.refreshInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(newCode, newFuture, entry.refreshInterval));  // 更新
        log.info("[管理系统-安全码] 安全码已使用并刷新 - UsedCodeId: {}, UsedCode: {}", codeId, usedCode);
        return usedCode;
    }

    /**
     * 刷新安全码
     * @param codeId 安全码标识
     * @return 新的安全码值
     */
    public String refreshCode(String codeId) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.get(codeId);
        String newCode = UUID.randomUUID().toString();  // 生成新安全码
        codeEntries.put(codeId, new CodeEntry(newCode, entry.future, entry.refreshInterval));  // 更新
        log.info("[管理系统-安全码] 安全码已刷新 - CodeId: {}, Code: {}", codeId, newCode);
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

    /**
     * 更新安全码刷新间隔
     * @param codeId 安全码标识
     * @param newInterval 新的刷新间隔 (ms)
     */
    public void updateInterval(String codeId, long newInterval) {
        codeExistenceValidation(codeId);
        if (newInterval <= 0) throw new IllegalArgumentException("参数格式错误");
        CodeEntry entry = codeEntries.get(codeId);
        if (entry.future != null) entry.future.cancel(false);  // 取消当前调度
        ScheduledFuture<?> newFuture = scheduler.schedule(  // 重新调度
                () -> {
                    refreshCode(codeId);
                    scheduleNext(codeId);
                },
                newInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(entry.code, newFuture, newInterval));  // 更新
        log.info("[管理系统-安全码] 安全码刷新间隔已更新 - CodeId: {}, NewInterval: {} ms", codeId, newInterval);
    }

    // =================== 工具方法 ===================

    private void codeExistenceValidation(String codeId) {
        if (!codeEntries.containsKey(codeId)) throw new IllegalArgumentException("安全码不存在");
    }

    private void scheduleNext(String codeId) {
        codeExistenceValidation(codeId);
        CodeEntry entry = codeEntries.get(codeId);
        ScheduledFuture<?> nextFuture = scheduler.schedule(  // 新调度
                () -> {
                    refreshCode(codeId);
                    scheduleNext(codeId);
                },
                entry.refreshInterval,
                TimeUnit.MILLISECONDS
        );
        codeEntries.put(codeId, new CodeEntry(entry.code, nextFuture, entry.refreshInterval));  // 更新
    }
}