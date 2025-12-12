package org.bot.nullbot.entity.game.delta;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;


@Data
@RequiredArgsConstructor
public class Player
{
    private Long userId;
    private String userName;
    private PlayerStatus status = PlayerStatus.IDLE;
    private LocalDateTime lastActive = LocalDateTime.now();

    public Player(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public enum PlayerStatus {
        IDLE, WAITING, PLAYING
    }
}