package org.bot.nullbot.entity.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameResult {

    private Boolean success;
    private Boolean isSameGroup;
    private Boolean isAsync;
    private Long selfGroupId;
    private Long opponentGroupId;
    private String selfInfo;
    private String opponentInfo;

    public static GameResult error(String info) {
        return new GameResult(
                false,
                null,
                null,
                null,
                null,
                info,
                null
        );
    }

    public static GameResult success(Boolean isSeperated, Long selfGroupId, Long opponentGroupId,
                                     String selfInfo, String opponentInfo) {
        return new GameResult(
                true,
                Objects.equals(selfGroupId, opponentGroupId),
                isSeperated,
                selfGroupId,
                opponentGroupId,
                selfInfo,
                opponentInfo
        );
    }
}
