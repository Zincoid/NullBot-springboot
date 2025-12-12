package org.bot.nullbot.command.game.tictactoe;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.MatchService;
import org.bot.nullbot.service.game.TicTacToeService;
import org.springframework.stereotype.Component;


@CommandMapping({"Action", "行动"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionCommand implements Command
{
    private final TicTacToeService ticTacToeService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(event.getCommandParameters().size() == 2) {
                Long groupId = groupMessageEvent.getGroupId();
                Long userId = groupMessageEvent.getUserId();
                String userName = bot.getStrangerInfo(userId, false).getData().getNickname();
                Long selfId = bot.getSelfId();

                int x = 0;
                int y = 0;

                try {
                    x = Integer.parseInt(event.getCommandParameters().get(0));
                    y = Integer.parseInt(event.getCommandParameters().get(1));
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[行动] ❌参数类型错误", false);
                    log.info("\t\t\t\t├─[Action] 参数类型错误");
                    return;
                }

                String result = ticTacToeService.move(userId, x, y);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result, false);
                log.info("\t\t\t\t├─[Action]  动作结果 - {}", result.replaceAll("\\R", ""));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[行动] ❌参数数量错误", false);
                log.info("\t\t\t\t├─[Action] 参数数量错误");
            }
        }else
            log.info("\t\t\t\t├─[Action] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Action 命令\n功能: 发送游戏动作\n限权: " + getAccess() + "\n格式: Action [命令]\n中文命令: 行动";
    }
}
