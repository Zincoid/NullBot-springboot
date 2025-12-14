package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class PlayerManager
{
    private final Map<Long, Player> playerMap = new ConcurrentHashMap<>();

    public Player getPlayer(Long userId) {
        return playerMap.get(userId);
    }

    public List<Player> getRecentPlayers(int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        return playerMap.values().stream()
                // 过滤掉 lastActionTime 为 null 的玩家
                .filter(player -> player.getLastActionTime() != null)
                // 按最后操作时间降序排序（最近的在前）
                .sorted(Comparator.comparing(Player::getLastActionTime).reversed())
                // 限制返回数量
                .limit(Math.min(count, playerMap.size()))
                .collect(Collectors.toList());
    }

    public Player refreshAndGetPlayer(Long userId, Long groupId, String userName) {
        Player player = playerMap.computeIfAbsent(userId, id -> {
            Player p = new Player();
            p.setUserId(id);
            p.setUserName(userName);
            p.setInProgressMatchId(null);
            return p;
        });
        if (player.getStatus() == Player.PlayerStatus.IDLE) {
            player.setGroupId(groupId);
            player.setLastActionTime(LocalDateTime.now());
        }
        return player;
    }


    public void updateStatus(Player player, Player.PlayerStatus status) {
        player.setStatus(status);
        player.setLastActionTime(LocalDateTime.now());
    }

    public void resetPlayer(Player player) {
        player.setStatus(Player.PlayerStatus.IDLE);
        player.setLastActionTime(LocalDateTime.now());
        player.setGroupId(null);
        player.setInProgressMatchId(null);
    }
}
