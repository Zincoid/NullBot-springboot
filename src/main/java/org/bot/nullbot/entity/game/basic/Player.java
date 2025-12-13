package org.bot.nullbot.entity.game.basic;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Player {
    private Long groupId;
    private Long userId;
    private String userName;
    private String inProgressMatchId;

    private LocalDateTime lastActionTime;
    private PlayerStatus status = PlayerStatus.IDLE;

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }
}