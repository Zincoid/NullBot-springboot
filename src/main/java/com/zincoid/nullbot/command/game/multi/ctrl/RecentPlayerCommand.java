package com.zincoid.nullbot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.annotation.CommandMapping;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.component.game.manager.PlayerManager;
import com.zincoid.nullbot.entity.game.basic.Player;
import com.zincoid.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@CommandMapping({"RecentPlayer", "最近玩家"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecentPlayerCommand implements Command {

    private final PlayerManager playerManager;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        List<Player> players = playerManager.getRecentPlayers(5);
        if (players == null || players.isEmpty())
            throw new NullBotMsgException("[最近玩家] ❌暂无记录");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder().append("[最近玩家] 当前状态-上次活跃");
        for (Player player : players) {
            sb.append("\n")
              .append(player.getUserName()).append("(").append(player.getUserId()).append(") :\n")
              .append(player.getStatus()).append(" ~ ").append(player.getLastActionTime().format(formatter));
        }
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("\t\t\t\t├─[RecentPlayer] 已获取");
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
