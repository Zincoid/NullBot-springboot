package com.zincoid.nullbot.bot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.game.Matcher;
import com.zincoid.nullbot.core.model.result.MatchResult;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Match", "匹配"})
@Component
@RequiredArgsConstructor
public class MatchCommand implements Command {

    private final Matcher matcher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String gameType = args.nextString();
        MatchResult result = matcher.joinMatch(userId, groupId, userName, gameType);
        if (result.getIsMatched() && !result.getIsSameGroup())
            bot.sendGroupMsg(result.getOpponentGroupId(), result.getInfo(), false);
        bot.sendGroupMsg(groupId, result.getInfo(), false);
        log.info("☑ [Match] 匹配结果 -> {}", result.getInfo());
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
