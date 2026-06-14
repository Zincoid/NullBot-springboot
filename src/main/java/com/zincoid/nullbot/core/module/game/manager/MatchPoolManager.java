package com.zincoid.nullbot.core.module.game.manager;

import com.zincoid.nullbot.core.module.game.model.Player;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Component
public class MatchPoolManager {

    // gameType -> waiting Queue
    private final Map<String, Queue<Player>> waitingPools = new ConcurrentHashMap<>();
    // playerId -> gameType  反向索引，避免 removePlayer 遍历所有队列
    private final Map<Long, String> playerGameTypeIndex = new ConcurrentHashMap<>();

    public void addPlayer(Player player, String gameType) {
        waitingPools.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>()).add(player);
        playerGameTypeIndex.put(player.getUserId(), gameType);
    }

    public Player pollPlayer(String gameType) {
        Queue<Player> queue = waitingPools.get(gameType);
        Player player = queue != null ? queue.poll() : null;
        if (player != null) {
            playerGameTypeIndex.remove(player.getUserId());
        }
        return player;
    }

    public boolean removePlayer(Player player) {
        String gameType = playerGameTypeIndex.remove(player.getUserId());
        if (gameType == null) return false;
        Queue<Player> queue = waitingPools.get(gameType);
        return queue != null && queue.remove(player);
    }

    public void removeTimeoutPlayers(long timeoutSeconds, Consumer<Player> onTimeout) {
        LocalDateTime now = LocalDateTime.now();
        waitingPools.forEach((gameType, queue) -> queue.removeIf(p -> {
            long seconds = Duration.between(p.getLastActionTime(), now).getSeconds();
            if (seconds >= timeoutSeconds) {
                playerGameTypeIndex.remove(p.getUserId());
                onTimeout.accept(p);
                return true;
            }
            return false;
        }));
    }
}
