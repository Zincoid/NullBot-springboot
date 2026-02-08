package org.bot.nullbot.command.schedule;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.TaskScheduler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@CommandMapping({"OneTimeAlarm", "一次性闹钟"})
@Component
@Slf4j
@RequiredArgsConstructor
public class OneTimeAlarmCommand implements Command
{
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd'T'HH:mm");
    private final TaskScheduler taskScheduler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();
            if (params.size() < 3)
                throw new NullBotMsgException("[一次性闹钟] ❌参数不足");

            String option = params.get(0);
            String message = params.get(2);
            String alarmId = "Alarm-%s-%s".formatted(userId, UUID.randomUUID().toString().substring(0, 8));

            try {
                switch (option)
                {
                    case "-t" -> {
                        LocalDateTime alarmTime = LocalDateTime.parse(params.get(1), formatter);
                        if (params.size() > 3) userId = Long.parseLong(params.get(3));
                        taskScheduler.setOneTimeGroupAtMsgAlarm(
                                alarmId,
                                groupId,
                                userId,
                                message,
                                alarmTime
                        );
                    }

                    case "-d" -> {
                        int delay = Integer.parseInt(params.get(1));
                        if (params.size() > 3) userId = Long.parseLong(params.get(3));
                        taskScheduler.setOneTimeGroupAtMsgAlarm(
                                alarmId,
                                groupId,
                                userId,
                                message,
                                LocalDateTime.now().plusMinutes(delay)
                        );
                    }

                    default -> throw new IllegalArgumentException("无此模式");
                }

            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[一次性闹钟] ❌参数格式错误");
            } catch (DateTimeParseException e) {
                throw new NullBotMsgException("[一次性闹钟] ❌时间格式错误");
            } catch (Exception e) {
                throw new NullBotMsgException("[一次性闹钟] ❌" + e.getMessage());
            }

            bot.sendGroupMsg(groupId, "[一次性闹钟] ⏰已设置！\n- AlarmID: " + alarmId, false);
            log.info("\t\t\t\t├─[OneTimeAlarm] 已设置 - AlarmID: {}", alarmId);
        }else
            throw new NullBotLogException("[一次性闹钟] ❌未设计 - 非群消息事件响应方式");
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
        return String.format("""
                ◉ OneTimeAlarm 命令
                功能: 设置一次性群内提醒闹钟
                限权: %d 级
                格式: OneTimeAlarm [模式] [时间] [文本] [目标QQ号]
                模式:
                - [-t] 时间模式
                  时间格式: yy-MM-ddTHH:mm
                - [-d] 延迟模式
                  时间格式: 分钟数
                示例:
                OneTimeAlarm -t 26-02-07T09:00 九点到了 2660181154
                OneTimeAlarm -d 10 十分钟了 2660181154""", getAccess()
        );
    }
}
