package com.zincoid.nullbot.bot.command.game.multi;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.module.game.Matcher;
import com.zincoid.nullbot.core.model.result.MatchResult;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"DisMatch", "取消匹配"})
@Component
@RequiredArgsConstructor
public class DisMatchCommand implements Command {

    private final Matcher matcher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        MatchResult result = matcher.cancelMatch(event.getUserId());
        bot.sendGroupMsg(event.getGroupId(), result.getInfo(), false);
        log.info("☑ [DisMatch] 取消匹配 -> {}", result.getInfo());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DisMatch 命令
                功能: 取消当前匹配
                限权: %d 级
                格式: DisMatch
                别名: 取消匹配""", getAccess()
        );
    }
}
