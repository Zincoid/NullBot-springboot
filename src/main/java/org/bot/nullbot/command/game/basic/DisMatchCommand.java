package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.MatchResult;
import org.springframework.stereotype.Component;

@CommandMapping({"DisMatch", "取消匹配"})
@Component
@Slf4j
@RequiredArgsConstructor
public class DisMatchCommand implements Command
{
    private final Matcher matcher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            MatchResult result = matcher.cancelMatch(userId);
            bot.sendGroupMsg(groupId, result.getInfo(), false);
            log.info("\t\t\t\t├─[DisMatch] 取消匹配结果 - {}", result.getInfo().replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[DisMatch] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ DisMatch 命令\n功能: 按游戏类型匹配\n限权: " + getAccess() + "\n格式: DisMatch\n中文命令: 取消匹配";
    }
}
