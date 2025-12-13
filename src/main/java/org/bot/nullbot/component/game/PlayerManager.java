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

    public Player getOrCreate(Long userId, Long groupId, String userName) {
        return playerMap.computeIfAbsent(
                userId,
                id -> {
                    Player p = new Player();
                    p.setUserId(userId);
                    p.setGroupId(groupId);
                    p.setUserName(userName);
                    p.setLastActionTime(LocalDateTime.now());
                    return p;
                }
        );
    }

    public void updateStatus(Player player, Player.PlayerStatus status) {
        player.setStatus(status);
        player.setLastActionTime(LocalDateTime.now());
    }

    public void resetPlayer(Player player) {
        player.setStatus(Player.PlayerStatus.IDLE);
        player.setInProgressMatchId(null);
    }
}
