package org.bot.nullbot.entity.game.basic;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
public class Player {
    private Long groupId;
    private Long userId;
    private String userName;
    private String inProgressMatchId;

    private LocalDateTime lastActive;
    private LocalDateTime waitingSince;
    private PlayerStatus status = PlayerStatus.IDLE;

    public Player(Long userId, Long groupId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.groupId = groupId;
    }

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }
}