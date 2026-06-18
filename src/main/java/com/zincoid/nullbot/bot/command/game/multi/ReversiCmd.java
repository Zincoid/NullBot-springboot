package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.model.Result;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.impl.reversi.ReversiHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Reversi", "黑白棋"})
@Component
@RequiredArgsConstructor
public class ReversiCmd implements Cmd {

    private final ReversiHandler reversiHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Result result = reversiHandler.act(event.getUserId(), args);
        result.send();
        log.info("☑ [Reversi] 黑白棋操作 -> {}", result.isOk());
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
