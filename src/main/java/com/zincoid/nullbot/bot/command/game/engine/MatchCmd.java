package com.zincoid.nullbot.bot.command.game.engine;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.GameEngine;
import com.zincoid.nullbot.core.module.game.model.Result;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Match", "匹配"})
@Component
@RequiredArgsConstructor
public class MatchCmd implements Cmd {

    private final GameEngine gameEngine;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String type = args.next();
        Result result = gameEngine.join(
                event.getGroupId(), event.getUserId(),
                event.getSender().getNickname(),
                type
        );
        result.send();
        log.info("☑ [Match] 匹配 -> {}", result.isOk());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Match 命令
                功能: 加入游戏匹配
                限权: %d 级
                格式: Match [类型]
                类型:
                - 井字棋 (TicTacToe)
                - 黑白棋 (Reversi)
                - 摸金 (Looting)
                别名: 匹配""", getAccess()
        );
    }
}
