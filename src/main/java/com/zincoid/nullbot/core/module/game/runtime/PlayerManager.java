package com.zincoid.nullbot.core.module.game.runtime;

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

    public Player set(Long userId, Long groupId, String userName) {
        Player player = playerMap.computeIfAbsent(userId, id -> Player.of(id, userName));
        if (player.getStatus() == Player.PlayerStatus.IDLE) {
            player.setInProgressGroupId(groupId);
            player.setLastActionTime(LocalDateTime.now());
        }
        return player;
    }

    public Player get(Long userId) {
        return playerMap.get(userId);
    }

    public void reset(Long userId) {
        update(userId, Player.PlayerStatus.IDLE);
    }
    
    public void update(Long userId, Player.PlayerStatus newStatus) {
        Player player = get(userId);
        if (player == null) throw new IllegalArgumentException("玩家不存在");
        Player.PlayerStatus oldStatus = player.getStatus();
        boolean valid = switch (oldStatus) {
            case IDLE -> newStatus == Player.PlayerStatus.WAITING
                    || newStatus == Player.PlayerStatus.PLAYING
                    || newStatus == Player.PlayerStatus.IDLE;
            case WAITING -> newStatus == Player.PlayerStatus.IDLE
                    || newStatus == Player.PlayerStatus.PLAYING;
            case PLAYING -> newStatus == Player.PlayerStatus.IDLE;
        };
        if (!valid) log.warn("玩家 {} 非法状态转换: {} -> {}", player.getId(), oldStatus, newStatus);
        player.setStatus(newStatus);
        player.setLastActionTime(LocalDateTime.now());
        if (newStatus == Player.PlayerStatus.IDLE || newStatus == Player.PlayerStatus.WAITING)
            player.setInProgressMatchId(null);
    }

    public List<Player> recent(int count) {
        if (count <= 0) return Collections.emptyList();
        return playerMap.values().stream()
                .filter(player -> player.getLastActionTime() != null)
                .sorted(Comparator.comparing(Player::getLastActionTime).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
