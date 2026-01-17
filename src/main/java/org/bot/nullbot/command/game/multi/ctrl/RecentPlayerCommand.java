package org.bot.nullbot.command.game.multi.ctrl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.manager.PlayerManager;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.Player;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@CommandMapping({"RecentPlayer", "最近玩家"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RecentPlayerCommand implements Command
{
    private final PlayerManager playerManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
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
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
            log.info("\t\t\t\t├─[RecentPlayer] 已获取 - {}", sb.toString().replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[最近玩家] ❌未设计 - 非群消息事件响应方式");
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
