package com.zincoid.nullbot.core.module.game.manager;

import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class PoolManager {

    private final Map<String, Queue<Player>> waitingPools = new ConcurrentHashMap<>();  // gameType -> waiting Queue
    private final Map<Long, String> playerGameTypeIndex = new ConcurrentHashMap<>();  // playerId -> gameType

    private final PlayerManager playerManager;

    public void add(Long userId, String gameType) {
        Player player = playerManager.get(userId);
        if (player == null) throw new RuntimeException("玩家未注册");
        waitingPools.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>()).add(player);
        playerGameTypeIndex.put(player.getId(), gameType);
    }

    public Player poll(String gameType) {
        Queue<Player> queue = waitingPools.get(gameType);
        Player player = queue != null ? queue.poll() : null;
        if (player != null) playerGameTypeIndex.remove(player.getId());
        return player;
    }

    public boolean remove(Long userId) {
        Player player = playerManager.get(userId);
        if (player == null) return false;
        String gameType = playerGameTypeIndex.remove(player.getId());
        if (gameType == null) return false;
        Queue<Player> queue = waitingPools.get(gameType);
        return queue != null && queue.remove(player);
    }

    public void clean(long timeoutSeconds, Consumer<Player> onTimeout) {
        LocalDateTime now = LocalDateTime.now();
        waitingPools.forEach((gameType, queue) -> queue.removeIf(p -> {
            long seconds = Duration.between(p.getLastActionTime(), now).getSeconds();
            if (seconds >= timeoutSeconds) {
                playerGameTypeIndex.remove(p.getId());
                onTimeout.accept(p);
                return true;
            }
            return false;
        }));
    }
}
