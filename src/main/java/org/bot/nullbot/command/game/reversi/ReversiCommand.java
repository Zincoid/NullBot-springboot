package org.bot.nullbot.command.game.reversi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.handler.ReversiMatchHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.GameResult;
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
            // 参数数量校验
            if (event.getCommandParameters().size() != 1) {
                bot.sendGroupMsg(
                        groupMessageEvent.getGroupId(),
                        "[黑白棋] ❌参数数量错误，示例：黑白棋 D3",
                        false
                );
                log.info("\t\t\t\t├─[Reversi] 参数数量错误");
                return;
            }
            String pos = event.getCommandParameters().getFirst().toUpperCase();
            // 坐标合法性校验
            if (!pos.matches("^[A-H][1-8]$")) {
                bot.sendGroupMsg(
                        groupMessageEvent.getGroupId(),
                        "[黑白棋] ❌坐标格式错误，应为 A1~H8",
                        false
                );
                log.info("\t\t\t\t├─[Reversi] 坐标格式错误：{}", pos);
                return;
            }
            // 执行落子
            GameResult result = reversiMatchHandler.move(groupMessageEvent.getUserId(), pos);
            // 跨群同步对手信息
            if (result.getSuccess() && !result.getIsSameGroup()) {
                bot.sendGroupMsg(
                        result.getOpponentGroupId(),
                        result.getInfo(),
                        false
                );
            }
            // 当前群消息
            bot.sendGroupMsg(
                    groupMessageEvent.getGroupId(),
                    result.getInfo(),
                    false
            );
            log.info(
                    "\t\t\t\t├─[Reversi] 落子结果 - {}",
                    result.getInfo().replaceAll("\\R", "")
            );
        } else {
            log.info("\t\t\t\t├─[Reversi] 未设计 - 非群消息事件响应方式");
        }
    }

    @Override
    public String getHelp() {
        return "◉ Reversi 命令\n" +
                "功能: 匹配成功后发送黑白棋落子指令\n" +
                "限权: " + getAccess() + "\n" +
                "格式: Reversi [坐标]\n" +
                "示例: Reversi D3\n" +
                "中文命令: 黑白棋";
    }
}
