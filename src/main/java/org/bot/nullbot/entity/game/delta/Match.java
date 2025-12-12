package org.bot.nullbot.entity.game.delta;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Match {
    private String matchId;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long player1Id;
    private String player1Name;
    private Long player2Id;
    private String player2Name;

    private MatchStatus status = MatchStatus.CREATED;
    private String gameData;

    public enum MatchStatus {
        CREATED, WAITING_PLAYER, READY, PLAYING, FINISHED, CANCELLED
    }

    /**
     * 创建副本（用于历史记录）
     */
    public Match copy() {
        Match copy = new Match();
        copy.setMatchId(this.matchId);
        copy.setCreateTime(this.createTime);
        copy.setStartTime(this.startTime);
        copy.setEndTime(this.endTime);
        copy.setPlayer1Id(this.player1Id);
        copy.setPlayer1Name(this.player1Name);
        copy.setPlayer2Id(this.player2Id);
        copy.setPlayer2Name(this.player2Name);
        copy.setStatus(this.status);
        copy.setGameData(this.gameData);
        return copy;
    }
}
