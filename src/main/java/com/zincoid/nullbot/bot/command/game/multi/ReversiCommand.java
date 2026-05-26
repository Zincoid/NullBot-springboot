package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.game.handler.impl.ReversiMatchHandler;
import com.zincoid.nullbot.core.model.result.GameResult;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Reversi", "黑白棋"})
@Component
@RequiredArgsConstructor
public class ReversiCommand implements Command {

    private final ReversiMatchHandler reversiMatchHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long userId = event.getUserId();
        String pos = args.nextString().toUpperCase();
        if (!pos.matches("^[A-H][1-8]$"))
            throw new NullBotException("坐标错误 范围: A1~H8");
        GameResult result = reversiMatchHandler.move(userId, pos);
        if (result.getSuccess()) {
            if (result.getIsAsync()) throw new NullBotException("该模式不发送异步消息");
            if (!result.getIsSameGroup())
                bot.sendGroupMsg(result.getOpponentGroupId(), result.getSelfInfo(), false);
            bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
        } else bot.sendGroupMsg(event.getGroupId(), result.getSelfInfo(), false);
        log.info("☑ [Reversi] 玩家 {} 落子 [{}]", userId, pos);
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
