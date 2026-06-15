package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.impl.tictactoe.TicTacToeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@CmdMapping({"TicTacToe", "井字棋"})
@Component
@RequiredArgsConstructor
public class TicTacToeCmd implements Cmd {

    private final TicTacToeHandler ticTacToeHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        ticTacToeHandler.act(event.getUserId(), args).send();
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
