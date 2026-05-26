package com.zincoid.nullbot.bot.command.schedule;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.BotTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@CommandMapping({"OneTimeAlarm", "一次性闹钟"})
@Component
@Slf4j
@RequiredArgsConstructor
public class OneTimeAlarmCommand implements Command {

    private final BotTaskScheduler botTaskScheduler;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final List<DateTimeFormatter> formatters = Arrays.asList(
            DateTimeFormatter.ofPattern("yy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME  // yyyy-MM-dd'T'HH:mm:ss
    );

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        if (params.size() < 3)
            throw new NullBotException("[一次性闹钟] ❌参数不足");

        String option = params.get(0);
        String message = params.get(2);
        String alarmId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime alarmTime;

        try {
            switch (option)
            {
                case "-t" -> {
                    alarmTime = parseDateTime(params.get(1), formatters);
                    if (params.size() > 3) userId = Long.parseLong(params.get(3));
                    botTaskScheduler.setOneTimeGroupAtMsgAlarm(
                            alarmId, groupId, userId, message, alarmTime
                    );
                }

                case "-d" -> {
                    int delay = Integer.parseInt(params.get(1));
                    alarmTime = LocalDateTime.now().plusMinutes(delay);
                    if (params.size() > 3) userId = Long.parseLong(params.get(3));
                    botTaskScheduler.setOneTimeGroupAtMsgAlarm(
                            alarmId, groupId, userId, message, alarmTime
                    );
                }

                default -> throw new IllegalArgumentException("无此模式");
            }

        } catch (NumberFormatException e) {
            throw new NullBotException("[一次性闹钟] ❌参数格式错误");
        } catch (DateTimeParseException e) {
            throw new NullBotException("[一次性闹钟] ❌时间格式错误");
        } catch (Exception e) {
            throw new NullBotException("[一次性闹钟] ❌" + e.getMessage());
        }

        bot.sendGroupMsg(groupId, """
                    [一次性闹钟] ⏰已设置！
                    - AlarmID: %s
                    - Time: %s""".formatted(alarmId, alarmTime.format(formatter)),
                false
        );
        log.info("├─[OneTimeAlarm] 已设置 - AlarmID: {}", alarmId);
    }

    private LocalDateTime parseDateTime(String str, List<DateTimeFormatter> formatters) {
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(str, formatter);
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }
        throw new DateTimeParseException("格式不匹配: " + str, str, 0);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ OneTimeAlarm 命令
                功能: 设置一次性群内提醒闹钟
                限权: %d 级
                格式: OneTimeAlarm [模式] [时间] [文本] [可选: QQ号]
                模式:
                - [-t] 时间模式
                  时间格式: yy-MM-ddTHH:mm
                  时间示例: 26-02-07T09:00
                - [-d] 延迟模式
                  时间格式: 分钟数
                别名: 一次性闹钟""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ OneTimeAlarm 命令
                功能: 设置一次性群内提醒闹钟
                格式: OneTimeAlarm [模式] [时间] [文本] [目标QQ号]
                模式:
                - [-t] 时间模式
                  时间格式: yy-MM-ddTHH:mm
                - [-d] 延迟模式
                  时间格式: 分钟数
                示例:
                OneTimeAlarm -t 26-02-07T09:00 九点到了 2660181154
                OneTimeAlarm -d 10 十分钟了 2660181154""";
    }
}
