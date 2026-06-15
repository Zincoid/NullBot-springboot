package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MatchingPool {

    private final Map<String, Queue<Player>> waitingPools = new ConcurrentHashMap<>();  // type -> waiting Queue
    private final Map<Long, String> playertypeIndex = new ConcurrentHashMap<>();  // playerId -> type

    private final PlayerManager playerManager;

    public void add(Long userId, String type) {
        Player player = playerManager.get(userId);
        if (player == null) throw new RuntimeException("玩家未注册");
        waitingPools.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(player);
        playertypeIndex.put(player.getId(), type);
    }

    public Player poll(String type) {
        Queue<Player> queue = waitingPools.get(type);
        Player player = queue != null ? queue.poll() : null;
        if (player != null) playertypeIndex.remove(player.getId());
        return player;
    }

    public boolean remove(Long userId) {
        Player player = playerManager.get(userId);
        if (player == null) return false;
        String type = playertypeIndex.remove(player.getId());
        if (type == null) return false;
        Queue<Player> queue = waitingPools.get(type);
        return queue != null && queue.remove(player);
    }

    public void clean(long timeoutSeconds, Consumer<Player> onTimeout) {
        List<Long> timedOutIds = new ArrayList<>();
        waitingPools.values().forEach(q -> q.forEach(p -> {
            long seconds = Duration.between(p.getLastActionTime(), LocalDateTime.now()).getSeconds();
            if (seconds < timeoutSeconds) return;
            onTimeout.accept(p);
            timedOutIds.add(p.getId());
        }));
        timedOutIds.forEach(this::remove);
    }
}
