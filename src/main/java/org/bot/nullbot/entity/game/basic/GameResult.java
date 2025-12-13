package org.bot.nullbot.entity.game.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResult
{
    private Boolean success;
    private Boolean isSameGroup;
    private Long opponentGroupId;
    private String info;

    public static GameResult error(String info) {
        return new GameResult(false, null, null, info);
    }

    public static GameResult success(Long opponentGroupId, String info) {
        if (opponentGroupId != null)
            return new GameResult(true, false, opponentGroupId, info);
        else
            return new GameResult(true, true, null, info);
    }
}
