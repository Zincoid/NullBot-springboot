package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.Matcher;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"FinishMatch", "终止对局"})
@Component
@RequiredArgsConstructor
public class FinishMatchCmd implements Cmd {

    private final Matcher matcher;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        if(!matcher.finishMatch(event.getUserId()))
            throw new BotInfoException(Emoji.INFO, "对局未找到");
        log.info("☑ [FinishMatch] 对局已终止");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ FinishMatch 命令
                功能: 强制终止自身所处对局
                限权: %d 级
                格式: FinishMatch
                别名: 终止对局""", getAccess()
        );
    }
}
