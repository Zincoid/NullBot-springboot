package org.bot.nullbot.entity.game.basic;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Match {
    private String matchId;
    private String gameType;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Player player1;
    private Player player2;

    private MatchStatus status = MatchStatus.CREATED;

    public enum MatchStatus {
        CREATED, WAITING, PLAYING, FINISHED
    }
}
