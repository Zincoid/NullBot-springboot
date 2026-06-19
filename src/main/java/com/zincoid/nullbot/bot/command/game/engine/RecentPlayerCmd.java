package com.zincoid.nullbot.bot.command.game.engine;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.Player;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@CmdMapping({"RecentPlayer", "最近玩家"})
@Component
@RequiredArgsConstructor
public class RecentPlayerCmd implements Cmd {

    private final PlayerManager playerManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        List<Player> players = playerManager.recent(5);
        if (players.isEmpty())
            throw new BotInfoException(Emoji.INFO, "暂无记录");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder().append("[最近玩家] 当前状态-上次活跃");
        for (Player player : players)
            sb.append("\n").append(player.getName()).append("(").append(player.getId()).append(") :\n")
              .append(player.getStatus()).append(" ~ ").append(player.getLastActionTime().format(formatter));
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("☑ [RecentPlayer] 最近玩家已获取 - Players: {}", players.size());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RecentPlayer 命令
                功能: 展示最近5个活跃玩家
                限权: %d 级
                格式: RecentPlayer
                别名: 最近玩家""", getAccess()
        );
    }
}
