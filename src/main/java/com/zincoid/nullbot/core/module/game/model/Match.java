package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Match {

    private String matchId;
    private String gameType;
    private Player player1;
    private Player player2;

    private LocalDateTime createTime;
    private LocalDateTime endTime;

    private LocalDateTime lastActionTime;
    private MatchStatus status = MatchStatus.CREATED;

    public enum MatchStatus {
        CREATED, PLAYING, FINISHED
    }

    // 用于快速获取对方群号
    public Long getOpponentGroupIdBySelfId(Long id) {
        return Objects.equals(id, player1.getUserId()) ? player2.getGroupId() : player1.getGroupId();
    }

    // 用于快速获取自己群号
    public Long getSelfGroupIdBySelfId(Long id) {
        return Objects.equals(id, player1.getUserId()) ? player1.getGroupId() : player2.getGroupId();
    }
}
