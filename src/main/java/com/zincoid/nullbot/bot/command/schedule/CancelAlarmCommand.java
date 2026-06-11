package com.zincoid.nullbot.bot.command.schedule;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.module.control.BotTaskScheduler;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"CancelAlarm", "取消闹钟"})
@Component
@RequiredArgsConstructor
public class CancelAlarmCommand implements Command {

    private final BotTaskScheduler botTaskScheduler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String alarmId = args.nextString();
        String taskId = "Alarm-%s-%s".formatted(userId, alarmId);
        if (!botTaskScheduler.cancelTask(taskId)) throw new BotInfoException(Emoji.INFO, "闹钟不存在");
        bot.sendGroupMsg(groupId, "✅闹钟已取消", false);
        log.info("☑ [CancelAlarm] 闹钟已取消 - AlarmID: {}", alarmId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ CancelAlarm 命令
                功能: 取消个人闹钟
                限权: %d 级
                格式: CancelAlarm [AlarmID]
                别名: 取消闹钟""", getAccess()
        );
    }
}
