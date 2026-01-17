package org.bot.nullbot.component.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SecurityCodeScheduler
{
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    private String activationCode;
    private static final long REFRESH_INTERVAL = 600_000;  // 10 Min

    public SecurityCodeScheduler() {
        this.activationCode = UUID.randomUUID().toString();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduleNextRefresh();  // 初始调度
    }

    /**
     * 使用并刷新激活码
     */
    public void useAndRefreshActivationCode() {
        String usedCode = this.activationCode;
        // 取消当前计时
        if (scheduledFuture != null) scheduledFuture.cancel(false);
        // 刷新激活码
        refreshActivationCode();
        // 重新开始计时
        scheduleNextRefresh();
        log.info("[管理系统-安全码] 激活码已使用并刷新 - Used: {}", usedCode);
    }

    /**
     * 获取当前激活码
     */
    public String getCurrentActivationCode() {
        log.info("[管理系统-安全码] 激活码已获取 - {}", activationCode);
        return activationCode;
    }

    /**
     * 仅刷新激活码
     */
    public void refreshActivationCode() {
        activationCode = UUID.randomUUID().toString();
        log.info("[管理系统-安全码] 激活码已刷新 - {}", activationCode);
    }

    /**
     * 计划下次刷新
     */
    private void scheduleNextRefresh() {
        scheduledFuture = scheduler.schedule(
                () -> {
                    refreshActivationCode();
                    scheduleNextRefresh(); // 递归调度
                },
                REFRESH_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }
}
