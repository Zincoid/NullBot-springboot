package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.game.handler.TicTacToeMatchHandler;
import com.zincoid.nullbot.core.model.result.GameResult;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"TicTacToe", "井字棋"})
@Component
@RequiredArgsConstructor
public class TicTacToeCmd implements Cmd {

    private final TicTacToeMatchHandler ticTacToeMatchHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long userId = event.getUserId();
        int x = args.nextInt();
        int y = args.nextInt();
        log.info("☑ [TicTacToe] 玩家 {} 落子 [{}, {}]", userId, x, y);
        GameResult result = ticTacToeMatchHandler.move(userId, x - 1, y - 1);

        if (!result.getSuccess()) {
            bot.sendGroupMsg(event.getGroupId(), result.getSelfInfo(), false);
            return;
        }
        if (result.getIsAsync()) throw new BotWarnException("游戏不支持异步消息");
        if (!result.getIsSameGroup())
            bot.sendGroupMsg(result.getOpponentGroupId(), result.getSelfInfo(), false);
        bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ TicTacToe 命令
                功能: 匹配成功后发送井字棋落子
                奖励: 30抽数 & 100Exp
                限权: %d 级
                格式: TicTacToe [行] [列]
                示例: TicTacToe 1 1
                别名: 井字棋""", getAccess()
        );
    }
}
