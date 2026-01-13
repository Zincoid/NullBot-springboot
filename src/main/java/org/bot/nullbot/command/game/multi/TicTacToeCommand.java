package org.bot.nullbot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.TicTacToeMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.result.GameResult;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

@CommandMapping({"TicTacToe", "井字棋"})
@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeCommand implements Command
{
    private final TicTacToeMatchHandler ticTacToeMatchHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() != 2)
                throw new NullBotMsgException("[井字棋] ❌参数数量错误 示例: 井字棋 1 1");

            int x, y;
            try {
                x = Integer.parseInt(event.getCommandParameters().get(0));
                y = Integer.parseInt(event.getCommandParameters().get(1));
            } catch (NumberFormatException ex) {
                throw new NullBotMsgException("[井字棋] ❌参数必须为数字");
            }

            GameResult result = ticTacToeMatchHandler.move(groupMessageEvent.getUserId(), x - 1, y - 1);

            if(result.getSuccess()){
                if(result.getIsAsync()) throw new NullBotMsgException("[井字棋] ❌该模式不发送异步消息");
                if(!result.getIsSameGroup())
                    bot.sendGroupMsg(result.getOpponentGroupId(), result.getSelfInfo(), false);
                bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
            }else
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result.getSelfInfo(), false);

            log.info("\t\t\t\t├─[TicTacToe] 落子 - {} {}", x, y);
        }else
            throw new NullBotLogException("[井字棋] ❌未设计 - 非群消息事件响应方式");
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
                中文命令: 井字棋""", getAccess()
        );
    }
}
