package org.bot.nullbot.command.timer;

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

import java.util.List;

@CommandMapping({"CancelAlarm", "取消闹钟"})
@Component
@Slf4j
@RequiredArgsConstructor
public class CancelAlarmCommand implements Command
{
    private final TaskScheduler taskScheduler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();

            if (params.isEmpty())
                throw new NullBotMsgException("[取消闹钟] ❌参数不足");
            String alarmId = params.getFirst();
            if (!alarmId.contains("Alarm-" + userId.toString()))
                throw new NullBotMsgException("[取消闹钟] ❌非法闹钟");

            boolean cancelled = taskScheduler.cancelTask(alarmId);

            bot.sendGroupMsg(groupId, "[取消闹钟] %s".formatted(cancelled ? "✅已取消" : "❌未取消"), false);
            log.info("\t\t\t\t├─[CancelAlarm] 闹钟取消{} - AlarmID: {}", cancelled ? "成功" : "失败", alarmId);
        }else
            throw new NullBotLogException("[取消闹钟] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ CancelAlarm 命令
                功能: 取消闹钟
                限权: %d 级
                格式: CancelAlarm [AlarmID]
                别名: 取消闹钟""", getAccess()
        );
    }
}
