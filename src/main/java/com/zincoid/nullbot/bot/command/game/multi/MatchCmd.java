package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.Matcher;
import com.zincoid.nullbot.core.model.result.MatchResult;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Match", "匹配"})
@Component
@RequiredArgsConstructor
public class MatchCmd implements Cmd {

    private final Matcher matcher;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        String gameType = args.nextString();
        MatchResult result = matcher.joinMatch(userId, groupId, userName, gameType);
        if (result.getIsMatched() && !result.getIsSameGroup())
            bot.sendGroupMsg(result.getOpponentGroupId(), result.getInfo(), false);
        bot.sendGroupMsg(groupId, result.getInfo(), false);
        log.info("☑ [Match] 匹配 -> {}", result.getInfo());
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
