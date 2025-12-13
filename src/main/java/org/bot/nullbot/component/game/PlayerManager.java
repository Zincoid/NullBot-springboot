package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerManager
{
    private final Map<Long, Player> playerMap = new ConcurrentHashMap<>();

    public Player getPlayer(Long userId) {
        return playerMap.get(userId);
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
