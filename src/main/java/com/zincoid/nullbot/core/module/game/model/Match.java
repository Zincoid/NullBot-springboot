package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Match {

    private final String matchId;
    private final String gameType;
    private final Player player1;
    private final Player player2;

    private final LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime lastActionTime = LocalDateTime.now();
    private LocalDateTime endTime;

    private MatchStatus status = MatchStatus.CREATED;

    public enum MatchStatus {
        CREATED, PLAYING, FINISHED
    }

    // 用于快速获取对方群号
    public Long getOpponentGroupIdBySelfId(Long id) {
        return Objects.equals(id, player1.getId()) ? player2.getInProgressGroupId() : player1.getInProgressGroupId();
    }

    // 用于快速获取自己群号
    public Long getSelfGroupIdBySelfId(Long id) {
        return Objects.equals(id, player1.getId()) ? player1.getInProgressGroupId() : player2.getInProgressGroupId();
    }
}
