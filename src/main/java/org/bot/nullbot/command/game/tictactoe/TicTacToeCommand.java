package org.bot.nullbot.command.game.tictactoe;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.MatchManager;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.GameResult;
import org.bot.nullbot.service.game.TicTacToeService;
import org.springframework.stereotype.Component;


@CommandMapping({"TicTacToe", "井字棋"})
@Component
@RequiredArgsConstructor
@Slf4j
public class TicTacToeCommand implements Command
{
    private final MatchManager matchManager;
    private final TicTacToeService ticTacToeService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(event.getCommandParameters().size() == 2) {
                int x, y;
                try {
                    x = Integer.parseInt(event.getCommandParameters().get(0));
                    y = Integer.parseInt(event.getCommandParameters().get(1));
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[井字棋] ❌参数类型错误", false);
                    log.info("\t\t\t\t├─[TicTacToe] 参数类型错误");
                    return;
                }
                GameResult result = ticTacToeService.move(groupMessageEvent.getUserId(), x, y);
                if(result.getSuccess() && !result.getIsSameGroup()){
                    bot.sendGroupMsg(result.getOpponentGroupId(), result.getInfo(), false);
                }
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result.getInfo(), false);
                log.info("\t\t\t\t├─[TicTacToe] 落子结果 - {}", result.getInfo().replaceAll("\\R", ""));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[井字棋] ❌参数数量错误", false);
                log.info("\t\t\t\t├─[TicTacToe] 参数数量错误");
            }
        }else
            log.info("\t\t\t\t├─[TicTacToe] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ TicTacToe 命令\n功能: 匹配成功后 发送井字棋游戏动作\n限权: " + getAccess() + "\n格式: TicTacToe [行] [列]\n中文命令: 井字棋";
    }
}
