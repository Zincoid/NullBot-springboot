package com.zincoid.nullbot.bot.command.game.base;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.service.base.UserService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@CmdMapping({"CheckIn", "签到"})
@Component
@RequiredArgsConstructor
public class CheckInCmd implements Cmd {

    private final Map<Long, LocalDateTime> checkInExpireMap = new ConcurrentHashMap<>();

    private final UserService userService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        LocalDateTime expireTime = checkInExpireMap.get(userId);
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            bot.sendGroupMsg(groupId, "今天签过啦！", false);
            log.info("☑ [CheckIn] 今日已签过到 - UserId: {}", userId);
            return;
        }
        userService.increaseDrawTimes(userId, 25);
        checkInExpireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
        bot.sendGroupMsg(groupId, """
                ✨%s 签到成功！获得:
                - 抽奖次数 x 25""".formatted(userName), false);
        log.info("☑ [CheckIn] 用户签到成功 - UserId: {}", userId);
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
