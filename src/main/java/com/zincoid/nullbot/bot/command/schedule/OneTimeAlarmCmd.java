package com.zincoid.nullbot.bot.command.schedule;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.BotTaskScheduler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@CmdMapping({"OneTimeAlarm", "单次闹钟"})
@Component
@RequiredArgsConstructor
public class OneTimeAlarmCmd implements Cmd {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME  // yyyy-MM-dd'T'HH:mm:ss
    );

    private final BotTaskScheduler botTaskScheduler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        String message = args.next();
        Long userId = args.nextLong(event.getUserId());
        LocalDateTime alarmTime;
        String alarmId = UUID.randomUUID().toString().substring(0, 8);
        try {
            if (args.hasOpt("time", "t")) {
                alarmTime = parseDateTime(args.getOpt("time", "t"));
                botTaskScheduler.setOneTimeGroupAtMsgAlarm(alarmId, groupId, userId, message, alarmTime);
            } else if (args.hasOpt("delay", "d")) {
                int delay = Integer.parseInt(args.getOpt("delay", "d"));
                alarmTime = LocalDateTime.now().plusMinutes(delay);
                botTaskScheduler.setOneTimeGroupAtMsgAlarm(alarmId, groupId, userId, message, alarmTime);
            } else {
                throw new BotWarnException("无此模式");
            }
        } catch (DateTimeParseException e) {
            throw new BotWarnException("时间格式错误");
        }
        bot.sendGroupMsg(groupId, """
                    ⏰单次闹钟已设置
                    - AlarmID: %s
                    - Time: %s""".formatted(alarmId, alarmTime.format(FORMATTER)),
                false
        );
        log.info("☑ [OneTimeAlarm] 闹钟已设置 - AlarmID: {}", alarmId);
    }

    private LocalDateTime parseDateTime(String str) {
        for (DateTimeFormatter formatter : OneTimeAlarmCmd.FORMATTERS) {
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
                功能: 设置单次群内提醒闹钟
                限权: %d 级
                用法: OneTimeAlarm [选项] [文本] [可选: QQ号]

                选项:
                -t,--time=[时间]   时间模式
                -d,--delay=[分钟]  延迟模式

                注意:
                - 时间模式参数格式: yy-MM-ddTHH:mm
                - 延迟模式在指定时间后触发
                - 未设置用户时默认为自己
                别名: 单次闹钟""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ OneTimeAlarm 命令
                功能: 设置单次群内提醒闹钟
                用法: OneTimeAlarm [选项] [文本] [QQ号]

                选项:
                -t,--time=[时间]   时间模式
                -d,--delay=[分钟]  延迟模式

                注意:
                - 时间模式参数格式: yy-MM-ddTHH:mm
                - 延迟模式在指定时间后触发

                示例:
                OneTimeAlarm --time=26-02-07T09:00 九点到了 2660181154
                OneTimeAlarm --delay=10 十分钟了 2660181154""";
    }
}
