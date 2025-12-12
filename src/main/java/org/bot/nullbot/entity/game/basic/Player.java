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
    private PlayerStatus status = PlayerStatus.IDLE;
    private String inProgressMatchId = null;
    private LocalDateTime lastActive = LocalDateTime.now();

    public Player(Long userId, Long groupId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.groupId = groupId;
    }

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }
}