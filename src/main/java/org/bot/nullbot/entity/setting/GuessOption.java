package org.bot.nullbot.entity.setting;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GuessOption {

    private final Long groupId;

    private double guessCropRatio = 0.1;
    private double guessTransparentRatio = 0.75;
    private int guessPadding = 250;
}
