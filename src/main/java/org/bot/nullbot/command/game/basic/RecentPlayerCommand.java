package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.game.PlayerManager;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.game.basic.Player;
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
            List<Player> players = playerManager.getRecentPlayers(6);
            if (players != null && !players.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                StringBuilder sb = new StringBuilder().append("[最近玩家] 当前状态-上次活跃");
                for (Player player : players) {
                    sb.append("\n").append(player.getUserName()).append("(").append(player.getUserId()).append(") :\n").append(player.getStatus()).append(" ~ ").append(player.getLastActionTime().format(formatter));
                }
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
                log.info("\t\t\t\t├─[RecentPlayer] 已获取 - {}", sb.toString().replaceAll("\\R", " "));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[最近玩家] ❌暂无记录", false);
                log.info("\t\t\t\t├─[RecentPlayer] 暂无记录");
            }
        }else
            log.info("\t\t\t\t├─[RecentPlayer] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ RecentPlayer 命令\n功能: 获取最近活跃的6个玩家\n限权: " + getAccess() + "\n格式: RecentPlayer\n中文命令: 最近玩家";
    }
}
