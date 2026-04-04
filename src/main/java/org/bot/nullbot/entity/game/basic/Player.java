package org.bot.nullbot.entity.game.basic;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class Player
{
    private Long groupId;
    private Long userId;
    private String userName;
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
        return Objects.equals(userId, player.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);  // groupId 是动态的 不能用来哈希
    }
}