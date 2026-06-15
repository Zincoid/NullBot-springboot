package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
@Data
@RequiredArgsConstructor(staticName = "of")
public class Match {

    private final String id;
    private final String type;
    private final Player p1;
    private final Player p2;

    private final LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime lastActionTime = LocalDateTime.now();
    private LocalDateTime endTime;

    private MatchStatus status = MatchStatus.CREATED;

    public enum MatchStatus {
        CREATED, PLAYING, FINISHED
    }
}
