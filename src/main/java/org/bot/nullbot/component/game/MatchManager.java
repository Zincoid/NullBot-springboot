package org.bot.nullbot.component.game;

import org.bot.nullbot.entity.game.basic.Match;
import org.bot.nullbot.entity.game.basic.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MatchManager
{
    private final Map<String, Match> matchMap = new ConcurrentHashMap<>();

    // playerId → matchId，用于快速查找玩家所在的对局
    private final Map<Long, String> playerMatchIndex = new ConcurrentHashMap<>();


    public Match createMatch(String gameType, Player p1, Player p2) {
        Match match = new Match();
        match.setMatchId(UUID.randomUUID().toString());
        match.setGameType(gameType);
        match.setCreateTime(LocalDateTime.now());
        match.setPlayer1(p1);
        match.setPlayer2(p2);

        matchMap.put(match.getMatchId(), match);

        // 建立索引
        playerMatchIndex.put(p1.getUserId(), match.getMatchId());
        playerMatchIndex.put(p2.getUserId(), match.getMatchId());

        return match;
    }

    public Match getMatch(String id) {
        return matchMap.get(id);
    }

    public void finishMatch(String id) {
        Match match = matchMap.remove(id);
        if (match != null) {
            playerMatchIndex.remove(match.getPlayer1().getUserId());
            playerMatchIndex.remove(match.getPlayer2().getUserId());
        }
    }

    public String getMatchIdByPlayerId(Long playerId) {
        return playerMatchIndex.get(playerId);
    }
}

