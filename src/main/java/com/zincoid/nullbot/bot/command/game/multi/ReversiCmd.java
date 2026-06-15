package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.impl.reversi.ReversiHandler;
import org.springframework.stereotype.Component;

@CmdMapping({"Reversi", "黑白棋"})
@Component
@RequiredArgsConstructor
public class ReversiCmd implements Cmd {

    private final ReversiHandler reversiHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        reversiHandler.act(event.getUserId(), args).send();
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reversi 命令
                功能: 匹配成功后发送黑白棋落子指令
                奖励: 50抽数 & 200Exp
                限权: %d 级
                格式: Reversi [坐标]
                示例: Reversi D3
                别名: 黑白棋""", getAccess()
        );
    }
}
