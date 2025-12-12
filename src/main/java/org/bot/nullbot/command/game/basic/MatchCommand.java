package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.MatchService;
import org.springframework.stereotype.Component;


@CommandMapping({"Match", "匹配"})
@Component
@Slf4j
@RequiredArgsConstructor
public class MatchCommand implements Command
{
    private final MatchService matchService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                Long groupId = groupMessageEvent.getGroupId();
                Long userId = groupMessageEvent.getUserId();
                String userName = bot.getStrangerInfo(userId, false).getData().getNickname();
                String gameType = event.getCommandParameters().getFirst();
                String result = matchService.joinMatch(userId, groupId, userName, gameType);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result, false);
                log.info("\t\t\t\t├─[Match] 匹配结果 - {}", result.replaceAll("\\R", ""));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[匹配] ❌无游戏类型参数", false);
                log.info("\t\t\t\t├─[Match] 无游戏类型参数");
            }
        }else
            log.info("\t\t\t\t├─[Match] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Match 命令\n功能: 按游戏类型匹配\n限权: " + getAccess() + "\n格式: Match [游戏类型]\n游戏类型: 现在只有tictactoe\n中文命令: 匹配";
    }
}
