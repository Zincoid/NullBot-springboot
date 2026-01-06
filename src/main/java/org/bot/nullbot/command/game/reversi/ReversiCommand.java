package org.bot.nullbot.command.game.reversi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.ReversiMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.result.GameResult;
import org.springframework.stereotype.Component;

@CommandMapping({"Reversi", "黑白棋"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ReversiCommand implements Command
{
    private final ReversiMatchHandler reversiMatchHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() != 1) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[黑白棋] ❌参数数量错误，示例：黑白棋 D3", false);
                log.info("\t\t\t\t├─[Reversi] 参数数量错误");
                return;
            }
            String pos = event.getCommandParameters().getFirst().toUpperCase();
            if (!pos.matches("^[A-H][1-8]$")) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[黑白棋] ❌坐标格式错误，应为 A1~H8", false);
                log.info("\t\t\t\t├─[Reversi] 坐标格式错误：{}", pos);
                return;
            }
            GameResult result = reversiMatchHandler.move(groupMessageEvent.getUserId(), pos);

            if(result.getSuccess()){
                if(!result.getIsAsync()){
                    bot.sendGroupMsg(result.getSelfGroupId(), result.getSelfInfo(), false);
                    if(!result.getIsSameGroup())
                        bot.sendGroupMsg(result.getOpponentGroupId(), result.getSelfInfo(), false);
                }else
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[黑白棋] ❌该模式不发送异步消息", false);
            }else
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), result.getSelfInfo(), false);

            log.info("\t\t\t\t├─[Reversi] 落子 - {}", pos);
        } else
            log.info("\t\t\t\t├─[Reversi] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reversi 命令
                功能: 匹配成功后发送黑白棋落子指令
                限权: %d 级
                格式: Reversi [坐标]
                示例: Reversi D3
                中文命令: 黑白棋""", getAccess()
        );
    }
}
