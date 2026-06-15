package com.zincoid.nullbot.core.module.game.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Player {

    private Long id;
    private String name;

    private Long inProgressGroupId;
    private String inProgressMatchId;
    private LocalDateTime lastActionTime;

    private PlayerStatus status = PlayerStatus.IDLE;

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static Player of(Long userId, String userName) {
        Player player = new Player();
        player.setId(userId);
        player.setName(userName);
        return player;
    }
}