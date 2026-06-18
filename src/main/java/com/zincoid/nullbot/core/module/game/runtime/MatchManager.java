package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.core.module.game.model.match.DualMatch;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.game.model.match.SoloMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class MatchManager {

    private final Map<Long, String> matchIndex = new ConcurrentHashMap<>();
    private final Map<Long, List<String>> historyIndex = new ConcurrentHashMap<>();
    private final Map<String, Match> matchMap = new ConcurrentHashMap<>();
    private final Map<String, Match> historyMap = new ConcurrentHashMap<>();

    private final PlayerManager playerManager;

    public DualMatch createDual(Long p1Id, Long p2Id, String type) {
        Player p1 = playerManager.get(p1Id);
        if (p1 == null) throw new IllegalArgumentException("玩家1不存在");
        Player p2 = playerManager.get(p2Id);
        if (p2 == null) throw new IllegalArgumentException("玩家2不存在");
        DualMatch match = new DualMatch(UUID.randomUUID().toString(), type, p1, p2);
        matchIndex.put(p1.getId(), match.getId());
        matchIndex.put(p2.getId(), match.getId());
        matchMap.put(match.getId(), match);
        return match;
    }

    public SoloMatch createSolo(Long playerId, String type) {
        Player player = playerManager.get(playerId);
        if (player == null) throw new IllegalArgumentException("玩家不存在");
        SoloMatch match = new SoloMatch(UUID.randomUUID().toString(), type, player);
        matchIndex.put(player.getId(), match.getId());
        matchMap.put(match.getId(), match);
        return match;
    }

    public Match get(String matchId) {
        return matchMap.get(matchId);
    }

    public Match get(Long userId) {
        String matchId = matchIndex.get(userId);
        if (matchId != null) return matchMap.get(matchId);
        return null;
    }

    public void remove(String matchId) {
        Match match = matchMap.remove(matchId);
        if (match == null) return;
        for (Player p : match.getPlayers()) {
            matchIndex.remove(p.getId());
            historyIndex.computeIfAbsent(p.getId(), k -> new ArrayList<>()).add(matchId);
        }
        match.setStatus(Match.MatchStatus.FINISHED);
        match.setLastActionTime(LocalDateTime.now());
        match.setEndTime(LocalDateTime.now());
        historyMap.put(matchId, match);
    }

    public Collection<Match> history(Long userId) {
        List<String> matchIds = historyIndex.get(userId);
        if (matchIds == null) return List.of();
        return matchIds.stream()
                .map(historyMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Collection<Match> histories() {
        return historyMap.values();
    }

    public void update(String matchId, Match.MatchStatus status) {
        Match match = matchMap.get(matchId);
        if (match == null) throw new IllegalArgumentException("对局不存在");
        match.setStatus(status);
        match.setLastActionTime(LocalDateTime.now());
    }

    public void clean(long timeoutSeconds, Consumer<Match> onTimeout) {
        List<String> timedOutIds = new ArrayList<>();
        matchMap.forEach((matchId, match) -> {
            if (match.getStatus() != Match.MatchStatus.PLAYING) return;
            long seconds = Duration.between(match.getLastActionTime(), LocalDateTime.now()).getSeconds();
            if (seconds < timeoutSeconds) return;
            onTimeout.accept(match);
            timedOutIds.add(matchId);
        });
        timedOutIds.forEach(this::remove);
    }
}
