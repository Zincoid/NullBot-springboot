package org.bot.nullbot.command.daily;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@CommandMapping({"Wife"})
@Component
public class WifeCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(WifeCommand.class);
    private final Map<Long, Long> wifeMap = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> expireMap = new ConcurrentHashMap<>();

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            GroupMemberInfoResp wife;
            Long userId = groupMessageEvent.getUserId();
            LocalDateTime expireTime = expireMap.get(userId);
            if(expireTime == null || expireTime.isBefore(LocalDateTime.now())) {
                List<GroupMemberInfoResp> groupMemberList = bot.getGroupMemberList(groupMessageEvent.getGroupId()).getData();
                do {
                    int randomIndex = ThreadLocalRandom.current().nextInt(groupMemberList.size());
                    wife = groupMemberList.get(randomIndex);
                } while (Objects.equals(wife.getUserId(), groupMessageEvent.getUserId()));
                Long wifeId = wife.getUserId();
                wifeMap.put(userId, wifeId);
                expireMap.put(userId, LocalDate.now().atTime(LocalTime.MAX));
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Wife] 你的今日老婆是 " + wife.getNickname() + "(" + wifeId + ")", false);
                logger.info("\t\t\t\t├─[Wife] 今日老婆: {} -> {}", userId, wifeId);
            }else{
                Long wifeId = wifeMap.get(userId);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Wife] 今天已经选过了哦... 你的老婆是 " + bot.getStrangerInfo(wifeId, false).getData().getNickname() + "(" + wifeId + ")", false);
                logger.info("\t\t\t\t├─[Wife] 今日已选过老婆: {} -> {}", userId, wifeId);
            }
        }else
            logger.info("\t\t\t\t├─[Wife] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/Wife 命令\n功能: 今日老婆\n限权: 0\n格式: /Wife";
    }
}
