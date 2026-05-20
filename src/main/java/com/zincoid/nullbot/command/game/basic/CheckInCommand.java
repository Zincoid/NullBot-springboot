package com.zincoid.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CommandMapping({"CheckIn", "签到"})
@Component
@RequiredArgsConstructor
@Slf4j
public class CheckInCommand implements Command {

    private final Map<Long, LocalDateTime> checkInExpireMap = new ConcurrentHashMap<>();
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        LocalDateTime expireTime = checkInExpireMap.get(userId);
        if (expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
            userService.increaseDrawTimes(userId, 25);
            checkInExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
            bot.sendGroupMsg(event.getGroupId(), "✨" + userName + " 签到成功！获得: \n- 抽奖次数 x 25", false);
            log.info("\t\t\t\t├─[CheckIn] 签到成功 - 用户 {}", userId);
        } else {
            bot.sendGroupMsg(event.getGroupId(), userName + " 今日已签过到！", false);
            log.info("\t\t\t\t├─[CheckIn] 今日已签过到 - 用户 {}", userId);
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ CheckIn 命令
                功能: 每日签到
                限权: %d 级
                格式: CheckIn
                别名: 签到""", getAccess()
        );
    }
}
