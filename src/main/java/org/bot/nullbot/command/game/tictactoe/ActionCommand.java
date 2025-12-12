package org.bot.nullbot.command.game.tictactoe;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.component.game.MatchService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@CommandMapping({"Action", "行动"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionCommand implements Command
{
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final MatchService matchService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                Long groupId = groupMessageEvent.getGroupId();
                Long userId = groupMessageEvent.getUserId();
                String userName = bot.getStrangerInfo(userId, false).getData().getNickname();
                Long selfId = bot.getSelfId();

                String action = event.getCommandParameters().getFirst();
                // String result = handleAction(groupId, userId, userName, action);
                // bot.sendGroupMsg(groupMessageEvent.getGroupId(), result, false);
                // log.info("\t\t\t\t├─[Action]  动作结果 - {}", result.replaceAll("\\R", ""));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[行动] ❌无行动参数", false);
                log.info("\t\t\t\t├─[Action] 无行动参数");
            }
        }else
            log.info("\t\t\t\t├─[Action] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Action 命令\n功能: 发送游戏动作\n限权: " + getAccess() + "\n格式: Action [命令]\n中文命令: 行动";
    }
}
