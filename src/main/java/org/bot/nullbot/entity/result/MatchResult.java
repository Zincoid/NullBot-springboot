package org.bot.nullbot.entity.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

@Data
@AllArgsConstructor
public class MatchResult {

    private Boolean isMatched;
    private Boolean isSameGroup;
    private Long opponentGroupId;
    private String info;

    public static MatchResult notMatched(String info) {
        return new MatchResult(
                false,
                null,
                null,
                info
        );
    }

    public static MatchResult matched(Long selfGroupId, Long opponentGroupId, String info) {
        return new MatchResult(
                true,
                Objects.equals(selfGroupId, opponentGroupId),
                opponentGroupId,
                info
        );
    }
}
