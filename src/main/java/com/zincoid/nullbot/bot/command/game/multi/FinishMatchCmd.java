package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.model.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.GameEngine;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"FinishMatch", "终止对局"})
@Component
@RequiredArgsConstructor
public class FinishMatchCmd implements Cmd {

    private final GameEngine gameEngine;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Result result = gameEngine.finish(
                event.getGroupId(),
                event.getUserId()
        );
        result.send();
        log.info("☑ [FinishMatch] 对局终止 -> {}", result.isOk());
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
