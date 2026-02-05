package org.bot.nullbot.component.control;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
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
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> alarmTasks;

    public Timer() {
        scheduler = Executors.newScheduledThreadPool(5);
        alarmTasks = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        setupDefaultAlarms();
        log.info("▽ [Timer] 定时器已初始化");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("▽ [Timer] 定时器已关闭");
    }

    private void setupDefaultAlarms() {
        scheduleDailyAlarm("0721-alarm", 7, 21, 0, () -> {
            String message = "现在是07:21时间!!!\nCiallo(∠・ω< )⌒☆";
            log.info("▽ [Timer] {}", message);
            sendNotification(message);
        });
    }

    // =================== 调用方法 ===================

    /**
     * 设置一次性闹钟
     */
    public void setOneTimeAlarm(String alarmId, LocalDateTime alarmTime, Runnable task) {
        long delay = calculateDelay(alarmTime);
        if (delay > 0) {
            ScheduledFuture<?> future = scheduler.schedule(() -> {
                try {
                    task.run();
                } finally {
                    alarmTasks.remove(alarmId);
                }
            }, delay, TimeUnit.MILLISECONDS);

            alarmTasks.put(alarmId, future);
            System.out.println("闹钟设置成功：" + alarmId + "，将在 " + alarmTime + " 触发");
        }
    }

    /**
     * 设置每天重复闹钟
     */
    public void scheduleDailyAlarm(String alarmId, int hour, int minute, int second, Runnable task) {
        long initialDelay = calculateDelayForToday(hour, minute, second);
        long period = 24 * 60 * 60 * 1000L; // 24小时的毫秒数

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("每日闹钟触发：" + alarmId + "，时间：" + LocalDateTime.now());
            task.run();
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        alarmTasks.put(alarmId, future);
    }

    /**
     * 设置固定间隔闹钟
     * @param alarmId 闹钟ID
     * @param initialDelay 初始延迟（毫秒）
     * @param period 执行间隔（毫秒）
     * @param task 执行的任务
     */
    public void scheduleFixedRateAlarm(String alarmId, long initialDelay, long period, Runnable task) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                wrapWithLogging(alarmId, task),
                initialDelay,
                period,
                TimeUnit.MILLISECONDS
        );

        alarmTasks.put(alarmId, future);
    }

    /**
     * 设置工作日闹钟
     */
    public void scheduleWorkdayAlarm(String alarmId, int hour, int minute, Runnable task) {
        Runnable workdayTask = () -> {
            LocalDateTime now = LocalDateTime.now();
            int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

            if (dayOfWeek >= 1 && dayOfWeek <= 5) { // 周一到周五
                System.out.println("工作日闹钟触发：" + alarmId);
                task.run();
            } else {
                System.out.println("今天是周末，" + alarmId + " 闹钟跳过");
            }
        };

        scheduleDailyAlarm(alarmId, hour, minute, 0, workdayTask);
    }

    /**
     * 取消闹钟
     */
    public boolean cancelAlarm(String alarmId) {
        ScheduledFuture<?> future = alarmTasks.get(alarmId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            alarmTasks.remove(alarmId);
            System.out.println("取消闹钟 " + alarmId + ": " + (cancelled ? "成功" : "失败"));
            return cancelled;
        }
        return false;
    }

    /**
     * 获取所有闹钟状态
     */
    public Map<String, String> getAllAlarmsStatus() {
        Map<String, String> status = new HashMap<>();
        alarmTasks.forEach((id, future) -> {
            String state = future.isCancelled() ? "已取消" :
                    future.isDone() ? "已完成" :
                            "运行中";
            status.put(id, state);
        });
        return status;
    }

    // =================== 工具方法 ===================

    private long calculateDelay(LocalDateTime alarmTime) {
        return Duration.between(LocalDateTime.now(), alarmTime).toMillis();
    }

    private long calculateDelayForToday(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayAlarm = LocalDateTime.of(
                now.getYear(), now.getMonth(), now.getDayOfMonth(),
                hour, minute, second
        );

        // 如果今天的时间已经过了，就设置到明天
        if (now.isAfter(todayAlarm)) {
            todayAlarm = todayAlarm.plusDays(1);
        }

        return calculateDelay(todayAlarm);
    }

    private Runnable wrapWithLogging(String alarmId, Runnable task) {
        return () -> {
            try {
                System.out.println("[" + LocalDateTime.now() + "] 执行闹钟任务: " + alarmId);
                task.run();
            } catch (Exception e) {
                System.err.println("闹钟任务执行失败: " + alarmId);
                e.printStackTrace();
            }
        };
    }

    private void sendNotification(String message) {
        // 这里可以实现：
        // 1. 发送WebSocket通知
        // 2. 调用短信/邮件接口
        // 3. 调用消息队列
        System.out.println("发送通知: " + message);
    }
}
