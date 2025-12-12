package org.bot.nullbot.entity.game.delta;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Player {
    private Long groupId;
    private Long userId;
    private String userName;
    private PlayerStatus status = PlayerStatus.IDLE;
    private String inProgressMatchId = null;

    private LocalDateTime lastActive;

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }
}