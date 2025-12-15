package org.bot.nullbot.command.game.tictactoe;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.TicTacToeMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.springframework.stereotype.Component;

@CommandMapping({"TicTacToe", "井字棋"})
@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeCommand implements Command {

    private final TicTacToeMatchHandler ticTacToeMatchHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {

            if (event.getCommandParameters().size() != 2) {
                bot.sendGroupMsg(
                        groupMessageEvent.getGroupId(),
                        "[井字棋] ❌参数数量错误，示例：井字棋 1 1",
                        false
                );
                return;
            }

            int x, y;
            try {
                x = Integer.parseInt(event.getCommandParameters().get(0));
                y = Integer.parseInt(event.getCommandParameters().get(1));
            } catch (NumberFormatException ex) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[井字棋] ❌参数必须为数字", false);
                return;
            }

            GameResult result = ticTacToeMatchHandler.move(groupMessageEvent.getUserId(), x - 1, y - 1);

            if(result.getSuccess()){
                if(!result.getIsAsync()){
                    if(result.getIsSameGroup()){
                        bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
                    }else{
                        bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
                        bot.sendGroupMsg(result.getOpponentGroupId(), result.getSelfInfo(), false);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[井字棋] ❌该模式不发送异步消息", false);
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result.getSelfInfo(), false);
            }

            log.info("\t\t\t\t├─[TicTacToe] 落子 - {} {}", x, y);
        }
    }

    @Override
    public String getHelp() {
        return """
                ◉ TicTacToe 命令
                功能: 匹配成功后发送井字棋落子
                限权: %s
                格式: TicTacToe [行] [列]
                示例: TicTacToe 1 1
                中文命令: 井字棋""".formatted(getAccess());
    }
}
