package org.bot.nullbot.component.control;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
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
public class BotTaskScheduler
{
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> tasks;

    public BotTaskScheduler(BotContainer botContainer) {
        this.botContainer = botContainer;
        scheduler = Executors.newScheduledThreadPool(5);
        tasks = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        setDailyAllGroupMsgAlarm(
                "0721",
                """
                        现在是 07:21 时间！✨
                        Ciallo～(∠・ω< )⌒☆""",
                7, 21, 0
        );
        log.info("▽ [BotTaskScheduler] 任务调度器已初始化");
    }

    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) scheduler.shutdownNow();
        log.info("▽ [BotTaskScheduler] 任务调度器已关闭");
    }

    // =================== BOT方法 ===================

    public void setOneTimeGroupAtMsgAlarm(String alarmId, Long groupId, Long userId,
                                          String message, LocalDateTime alarmTime)
    {
        String taskId = "Alarm-%s-%s".formatted(userId, alarmId);
        setOneTimeTask(taskId, alarmTime, () -> {
            Bot bot = botContainer.robots.get(botId);
            bot.sendGroupMsg(groupId, "[CQ:at,qq=%s] %s".formatted(userId, message), false);
        });
    }

    public void setDailyGroupMsgAlarm(String alarmId, Long groupId, String message,
                                      int hour, int minute, int second)
    {
        String taskId = "Alarm-%s-%s".formatted(groupId, alarmId);
        setDailyTask(taskId, hour, minute, second, () -> {
            Bot bot = botContainer.robots.get(botId);
            bot.sendGroupMsg(groupId, message, false);
        });
    }

    public void setDailyAllGroupMsgAlarm(String alarmId, String message,
                                         int hour, int minute, int second)
    {
        String taskId = "Alarm-%s-%s".formatted("Global", alarmId);
        setDailyTask(taskId, hour, minute, second, () -> {
            Bot bot = botContainer.robots.get(botId);
            for (GroupInfoResp group : bot.getGroupList().getData())
                bot.sendGroupMsg(group.getGroupId(), message, false);
        });
    }

    // =================== 任务方法 ===================

    public void setOneTimeTask(String taskId, LocalDateTime alarmTime, Runnable task) {
        long delay = calculateDelay(alarmTime);
        if (delay > 0) {
            ScheduledFuture<?> future = scheduler.schedule(
                    wrapWithLogging(taskId, task, true),
                    delay,
                    TimeUnit.MILLISECONDS
            );
            tasks.put(taskId, future);
        } else
            throw new IllegalArgumentException("时间不合法");
    }

    public void setDailyTask(String taskId, int hour, int minute, int second, Runnable task) {
        long initialDelay = calculateDelay(hour, minute, second);
        long period = 24 * 60 * 60 * 1000L;
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                wrapWithLogging(taskId, task, false),
                initialDelay,
                period,
                TimeUnit.MILLISECONDS
        );
        tasks.put(taskId, future);
    }

    public boolean cancelTask(String taskId) {
        ScheduledFuture<?> future = tasks.get(taskId);
        if (future != null) {
            boolean cancelled = future.cancel(false);
            tasks.remove(taskId);
            return cancelled;
        }
        return false;
    }

    public Map<String, String> getTasks() {
        Map<String, String> status = new HashMap<>();
        tasks.forEach((id, future) -> {
            String state = future.isCancelled() ? "Cancelled" :
                    future.isDone() ? "Done" :
                            "Running";
            status.put(id, state);
        });
        return status;
    }

    // =================== 工具方法 ===================

    private long calculateDelay(LocalDateTime time) {
        return Duration.between(LocalDateTime.now(), time).toMillis();
    }

    private long calculateDelay(int hour, int minute, int second) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayTime = LocalDateTime.of(
                now.getYear(), now.getMonth(), now.getDayOfMonth(),
                hour, minute, second
        );
        if (now.isAfter(todayTime)) todayTime = todayTime.plusDays(1);
        return calculateDelay(todayTime);
    }

    private Runnable wrapWithLogging(String taskId, Runnable task, boolean remove) {
        return () -> {
            try {
                log.info("▽ [BotTaskScheduler] {} - {} 任务开始执行", LocalDateTime.now(), taskId);
                task.run();
            } catch (Exception e) {
                log.error("▽ [BotTaskScheduler] {} - {} 任务执行出错", LocalDateTime.now(), taskId);
                throw e;
            } finally {
                if (remove) tasks.remove(taskId);
            }
        };
    }
}
