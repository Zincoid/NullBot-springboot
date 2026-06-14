package com.zincoid.nullbot.core.module.game.manager;

import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PlayerManager {

    private final Map<Long, Player> playerMap = new ConcurrentHashMap<>();

    public Player getPlayer(Long userId) {
        return playerMap.get(userId);
    }

    public List<Player> getRecentPlayers(int count) {
        if (count <= 0) return Collections.emptyList();
        return playerMap.values().stream()
                .filter(player -> player.getLastActionTime() != null)
                .sorted(Comparator.comparing(Player::getLastActionTime).reversed())
                .limit(count)
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
    
    public void updateStatus(Player player, Player.PlayerStatus newStatus) {
        Player.PlayerStatus oldStatus = player.getStatus();
        boolean valid = switch (oldStatus) {
            case IDLE -> newStatus == Player.PlayerStatus.WAITING
                    || newStatus == Player.PlayerStatus.PLAYING
                    || newStatus == Player.PlayerStatus.IDLE;
            case WAITING -> newStatus == Player.PlayerStatus.IDLE
                    || newStatus == Player.PlayerStatus.PLAYING;
            case PLAYING -> newStatus == Player.PlayerStatus.IDLE;
        };
        if (!valid) {
            log.warn("玩家 {} 非法状态转换: {} -> {}", player.getUserId(), oldStatus, newStatus);
        }
        player.setStatus(newStatus);
        player.setLastActionTime(LocalDateTime.now());
    }

    public void resetPlayer(Player player) {
        updateStatus(player, Player.PlayerStatus.IDLE);
        player.setInProgressMatchId(null);
    }
}
