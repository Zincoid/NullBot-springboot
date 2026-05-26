package com.zincoid.nullbot.bot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.game.Matcher;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"FinishMatch", "终止对局"})
@Component
@RequiredArgsConstructor
public class FinishMatchCommand implements Command {

    private final Matcher matcher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        if(!matcher.finishMatch(event.getUserId()))
            throw new BotWarnException("未找到对局");
        log.info("☑ [FinishMatch] 对局已终止");
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
