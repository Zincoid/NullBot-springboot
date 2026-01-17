package org.bot.nullbot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.Matcher;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

@CommandMapping({"FinishMatch", "终止对局"})
@Component
@Slf4j
@RequiredArgsConstructor
public class FinishMatchCommand implements Command
{
    private final Matcher matcher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getUserId();
            if(!matcher.finishMatch(userId))
                throw new NullBotMsgException("[终止对局] ❌未找到玩家/对局");
            log.info("\t\t\t\t├─[FinishMatch] 终止对局结果 - 已终止");
        } else
            throw new NullBotLogException("[终止对局] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FinishMatch 命令
                功能: 强制终止自身正在进行的对局
                限权: %d 级
                格式: FinishMatch
                别名: 终止对局""", getAccess()
        );
    }
}
