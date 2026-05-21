package com.zincoid.nullbot.core.component.game.manager;

import com.zincoid.nullbot.core.model.game.basic.Player;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class MatchPoolManager {

    // gameType -> waiting Queue
    private final Map<String, Queue<Player>> waitingPools = new ConcurrentHashMap<>();

    public void addPlayer(Player player, String gameType) {
        waitingPools.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>()).add(player);
    }

    public Player pollPlayer(String gameType) {
        Queue<Player> queue = waitingPools.computeIfAbsent(gameType, k -> new ConcurrentLinkedQueue<>());
        return queue.isEmpty() ? null : queue.poll();
    }

    public boolean removePlayer(Player player) {
        for (Queue<Player> queue : waitingPools.values())
            if (queue.remove(player))
                return true;
        return false;
    }

    public Map<String, Queue<Player>> getAllPools() {
        return waitingPools;
    }
}
