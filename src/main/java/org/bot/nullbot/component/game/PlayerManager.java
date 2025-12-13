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

    public Player refreshPlayer(Long userId, Long groupId, String userName) {
        Player player = playerMap.get(userId);
        if (player == null) {
            Player p = new Player();
            p.setUserId(userId);
            p.setGroupId(groupId);
            p.setUserName(userName);
            p.setInProgressMatchId(null);
            p.setLastActionTime(LocalDateTime.now());
            playerMap.put(userId, p);
            return p;
        } else {
            if (player.getStatus() != Player.PlayerStatus.IDLE) { return player; }
            player.setGroupId(groupId);
            player.setLastActionTime(LocalDateTime.now());
            playerMap.put(userId, player);
            return player;
        }
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
