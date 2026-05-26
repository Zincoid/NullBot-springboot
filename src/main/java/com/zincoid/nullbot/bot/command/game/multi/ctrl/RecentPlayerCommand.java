package com.zincoid.nullbot.bot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.game.manager.PlayerManager;
import com.zincoid.nullbot.core.model.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@CommandMapping({"RecentPlayer", "最近玩家"})
@Component
@RequiredArgsConstructor
public class RecentPlayerCommand implements Command {

    private final PlayerManager playerManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        List<Player> players = playerManager.getRecentPlayers(5);
        if (players == null || players.isEmpty())
            throw new NullBotException("暂无记录");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder().append("[最近玩家] 当前状态-上次活跃");
        for (Player player : players) {
            sb.append("\n")
              .append(player.getUserName()).append("(").append(player.getUserId()).append(") :\n")
              .append(player.getStatus()).append(" ~ ").append(player.getLastActionTime().format(formatter));
        }
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("☑ [RecentPlayer] 已获取");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RecentPlayer 命令
                功能: 展示最近活跃的5个玩家
                限权: %d 级
                格式: RecentPlayer
                别名: 最近玩家""", getAccess()
        );
    }
}
