package com.zincoid.nullbot.core.module.game.manager;

import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MatchManager {

    private final Map<Long, String> playerMatchIndex = new ConcurrentHashMap<>();
    private final Map<String, Match> matchMap = new ConcurrentHashMap<>();
    private final Map<String, Match> historyMap = new ConcurrentHashMap<>();

    private final PlayerManager playerManager;

    public Match create(Long player2Id, Long player1Id, String gameType) {
        Player p1 = playerManager.get(player1Id);
        if (p1 == null) throw new IllegalArgumentException("玩家1不存在");
        Player p2 = playerManager.get(player2Id);
        if (p2 == null) throw new IllegalArgumentException("玩家2不存在");
        Match match = Match.of(UUID.randomUUID().toString(), gameType, p1, p2);
        playerMatchIndex.put(p1.getId(), match.getMatchId());
        playerMatchIndex.put(p2.getId(), match.getMatchId());
        matchMap.put(match.getMatchId(), match);
        return match;
    }

    public void remove(String matchId) {
        Match match = matchMap.remove(matchId);
        if (match == null) return;
        match.setStatus(Match.MatchStatus.FINISHED);
        match.setEndTime(LocalDateTime.now());
        match.setLastActionTime(LocalDateTime.now());
        historyMap.put(matchId, match);
        playerMatchIndex.remove(match.getPlayer1().getId());
        playerMatchIndex.remove(match.getPlayer2().getId());
    }

    public Match get(String matchId) {
        return matchMap.get(matchId);
    }

    public Match get(Long userId) {
        String matchId = playerMatchIndex.get(userId);
        if (matchId != null) return matchMap.get(matchId);
        return null;
    }

    public Collection<Match> getAll() {
        return matchMap.values();
    }

    public Collection<Match> history(Long userId) {
        return historyMap.values().stream()
                .filter(m -> m.getPlayer1().getId().equals(userId)
                        || m.getPlayer2().getId().equals(userId))
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
}

