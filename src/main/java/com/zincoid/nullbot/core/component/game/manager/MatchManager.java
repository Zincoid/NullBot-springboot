package com.zincoid.nullbot.core.component.game.manager;

import com.zincoid.nullbot.core.model.game.basic.Match;
import com.zincoid.nullbot.core.model.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchManager {

    // playerId -> matchId 用于快速查找玩家所在的对局
    private final Map<Long, String> playerMatchIndex = new ConcurrentHashMap<>();
    private final Map<String, Match> matchMap = new ConcurrentHashMap<>();

    public Match createMatch(String gameType, Player p1, Player p2) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID().toString());
        match.setGameType(gameType);
        match.setCreateTime(LocalDateTime.now());
        match.setLastActionTime(LocalDateTime.now());
        match.setPlayer1(p1);
        match.setPlayer2(p2);

        playerMatchIndex.put(p1.getUserId(), match.getMatchId());
        playerMatchIndex.put(p2.getUserId(), match.getMatchId());
        matchMap.put(match.getMatchId(), match);

        return match;
    }

    public void removeMatch(String matchId) {
        Match match = matchMap.remove(matchId);
        if (match != null) {
            match.setStatus(Match.MatchStatus.FINISHED);
            match.setEndTime(LocalDateTime.now());
            match.setLastActionTime(LocalDateTime.now());

            /* TODO: 保存对局历史 */

            playerMatchIndex.remove(match.getPlayer1().getUserId());
            playerMatchIndex.remove(match.getPlayer2().getUserId());
        }
    }

    public Match getMatch(String matchId) {
        return matchMap.get(matchId);
    }

    public Collection<Match> getAllMatches() {
        return matchMap.values();
    }

    public void updateMatchStatus(Match match, Match.MatchStatus status) {
        match.setStatus(status);
        match.setLastActionTime(LocalDateTime.now());
    }

    // 游戏服务工具

    public Match getMatchBySelfId(Long selfId) {
        String matchId = playerMatchIndex.get(selfId);
        if (matchId != null) {
            return matchMap.get(matchId);
        }else
            return null;
    }
}

