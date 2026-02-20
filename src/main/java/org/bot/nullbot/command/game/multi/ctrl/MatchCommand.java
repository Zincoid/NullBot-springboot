package org.bot.nullbot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.result.MatchResult;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;


@CommandMapping({"Match", "匹配"})
@Component
@Slf4j
@RequiredArgsConstructor
public class MatchCommand implements Command
{
    private final Matcher matcher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[匹配] ❌未指定游戏");

        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String gameType = params.getFirst();

        MatchResult result = matcher.joinMatch(userId, groupId, userName, gameType);
        if (result == null)
            throw new NullBotMsgException("[匹配] ❌未知错误");

        if (result.getIsMatched() && !result.getIsSameGroup())
            bot.sendGroupMsg(result.getOpponentGroupId(), result.getInfo(), false);
        bot.sendGroupMsg(groupId, result.getInfo(), false);
        log.info("\t\t\t\t├─[Match] 匹配结果 - {}", result.getInfo().replaceAll("\\R", " "));
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Match 命令
                功能: 按游戏类型匹配
                限权: %d 级
                格式: Match [游戏类型]
                游戏类型:
                - Tictactoe 井字棋
                - Reversi 黑白棋
                - Looting 摸金
                别名: 匹配""", getAccess()
        );
    }
}
