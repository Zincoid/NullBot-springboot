package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CommandMapping({"CheckIn", "签到"})
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckInCommand implements Command
{
    private final Map<Long, LocalDateTime> checkInExpireMap = new ConcurrentHashMap<>();
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            LocalDateTime expireTime = checkInExpireMap.get(userId);
            if(expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                userService.increaseDrawTimes(userId, 25);
                checkInExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "✨" + userName + " 签到成功！获得: \n- 抽奖次数 x 25", false);
                log.info("\t\t\t\t├─[CheckIn] 签到成功 - 用户 {}", userId);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), userName + " 今日已签过到！", false);
                log.info("\t\t\t\t├─[CheckIn] 今日已签过到 - 用户 {}", userId);
            }
        }else
            throw new NullBotLogException("[签到] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ CheckIn 命令
                功能: 每日签到
                限权: %d 级
                格式: CheckIn
                中文命令: 签到""", getAccess()
        );
    }
}
