package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
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
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[系统] ✨" + userName + " 签到成功！\n获得抽奖次数x25", false);
                log.info("\t\t\t\t├─[System.CheckIn] 签到成功 - 用户 {}", userId);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[系统] " + userName + " 今日已签过到！", false);
                log.info("\t\t\t\t├─[System.CheckIn] 今日已签过到 - 用户 {}", userId);
            }
        }else
            log.info("\t\t\t\t├─[System.CheckIn] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ CheckIn 命令\n功能: 每日签到\n限权: " + getAccess() + "\n格式: CheckIn\n中文命令: 签到";
    }
}
