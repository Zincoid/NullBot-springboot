package org.bot.nullbot.component.control;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
public class Timer
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> alarmTasks;

    public Timer(BotContainer botContainer) {
        this.botContainer = botContainer;
        scheduler = Executors.newScheduledThreadPool(5);
        alarmTasks = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        setDailyGroupMsgAlarm(
                "Alarm-0721-459358160",
                459358160L,
                "现在是07:21时间!!!\nCiallo(∠・ω< )⌒☆",
                7, 21, 0
        );
        setDailyGroupMsgAlarm(
                "Alarm-0721-364928377",
                364928377L,
                "现在是07:21时间!!!\nCiallo(∠・ω< )⌒☆",
                7, 21, 0
        );
        // setDailyGroupMsgAlarm(
        //         "Alarm-test-875310845",
        //         875310845L,
        //         "这是一条测试消息",
        //         0, 0, 0
        // );
        log.info("▽ [Timer] 定时器已初始化");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("▽ [Timer] 定时器已关闭");
    }

    // =================== 调用方法 ===================

    public void setDailyGroupMsgAlarm(String alarmId, Long groupId, String message,
                                      int hour, int minute, int second)
    {
        setDailyAlarm(alarmId, hour, minute, second, () -> {
            Bot bot = botContainer.robots.get(botId);
            bot.sendGroupMsg(groupId, message, false);
        });
    }

    // =================== 定时方法 ===================

    public void setOneTimeAlarm(String alarmId, LocalDateTime alarmTime, Runnable task) {
        long delay = calculateDelay(alarmTime);
        if (delay > 0) {
            ScheduledFuture<?> future = scheduler.schedule(
                    wrapWithLogging(alarmId, task, true),
                    delay,
                    TimeUnit.MILLISECONDS
            );
            alarmTasks.put(alarmId, future);
        } else
            throw new IllegalArgumentException("时间不合法");
    }

    public void setDailyAlarm(String alarmId, int hour, int minute, int second, Runnable task) {
        long initialDelay = calculateDelay(hour, minute, second);
        long period = 24 * 60 * 60 * 1000L;
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                wrapWithLogging(alarmId, task, false),
                initialDelay,
                period,
                TimeUnit.MILLISECONDS
        );
        alarmTasks.put(alarmId, future);
    }

    public boolean cancelAlarm(String alarmId) {
        ScheduledFuture<?> future = alarmTasks.get(alarmId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            alarmTasks.remove(alarmId);
            return cancelled;
        }
        return false;
    }

    public Map<String, String> getAlarms() {
        Map<String, String> status = new HashMap<>();
        alarmTasks.forEach((id, future) -> {
            String state = future.isCancelled() ? "Cancelled" :
                    future.isDone() ? "Done" :
                            "Running";
            status.put(id, state);
        });
        return status;
    }

    // =================== 工具方法 ===================

    private long calculateDelay(LocalDateTime alarmTime) {
        return Duration.between(LocalDateTime.now(), alarmTime).toMillis();
    }

    private long calculateDelay(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayAlarm = LocalDateTime.of(
                now.getYear(), now.getMonth(), now.getDayOfMonth(),
                hour, minute, second
        );
        if (now.isAfter(todayAlarm)) {
            todayAlarm = todayAlarm.plusDays(1);
        }
        return calculateDelay(todayAlarm);
    }

    private Runnable wrapWithLogging(String alarmId, Runnable task, boolean remove) {
        return () -> {
            try {
                log.info("▽ [Timer] {}: {} - 任务开始执行", LocalDateTime.now(), alarmId);
                task.run();
            } catch (Exception e) {
                log.error("▽ [Timer] {}: {} - 任务执行失败", LocalDateTime.now(), alarmId);
                throw e;
            } finally {
                if (remove) alarmTasks.remove(alarmId);
            }
        };
    }
}
