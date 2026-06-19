package com.zincoid.nullbot.core.enums;

import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Random;

@Getter
public enum Rps {

    PAPER(1, "布"),
    SCISSORS(2, "剪刀"),
    ROCK(3, "石头");

    private final int value;
    private final String desc;

    Rps(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public boolean judge(Rps other) {
        if (other == null) throw new BotWarnException("RPS为空");
        if (this == other) throw new BotWarnException("RPS平局");
        return switch (other) {
            case PAPER -> this == SCISSORS;
            case SCISSORS -> this == ROCK;
            case ROCK -> this == PAPER;
        };
    }

    public static Rps of(int value) {
        return Arrays.stream(values())
                .filter(rps -> rps.value == value)
                .findFirst()
                .orElse(null);
    }

    public static Rps random() {
        return values()[new Random().nextInt(values().length)];
    }
}
