package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public abstract class Match {

    private final String id;
    private final String type;

    private final LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime lastActionTime = LocalDateTime.now();
    private LocalDateTime endTime;

    private MatchStatus status = MatchStatus.CREATED;

    public Match(String id, String type) {
        this.id = id;
        this.type = type;
    }

    public abstract List<Player> getPlayers();

    public enum MatchStatus {
        CREATED, PLAYING, FINISHED
    }
}
