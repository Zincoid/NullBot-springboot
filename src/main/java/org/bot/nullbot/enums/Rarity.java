package org.bot.nullbot.enums;

import lombok.Getter;

@Getter
public enum Rarity
{
    WHITE(0, "白", 0.15),
    GREEN(1, "绿", 0.15),
    BLUE(2, "蓝", 0.25),
    PURPLE(3, "紫", 0.35),
    GOLD(4, "金", 0.08),
    RED(5, "红", 0.02);

    private final int value;
    private final String name;
    private final double probability;

    Rarity(int value, String name, double probability) {
        this.value = value;
        this.name = name;
        this.probability = probability;
    }
}