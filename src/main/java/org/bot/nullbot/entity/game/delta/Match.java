package org.bot.nullbot.entity.game.delta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Match
{
    private String matchId;
    private MatchStatus status;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long player1Id;
    private Long player2Id;

    private String gameData;

    public enum MatchStatus {
        CREATED, WAITING_PLAYER, READY, PLAYING, FINISHED, CANCELLED
    }

    public Match(Long player1Id, Long player2Id) {
        this.matchId = "MATCH-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.createTime = LocalDateTime.now();
        this.status = MatchStatus.CREATED;
    }
}
