package com.zincoid.nullbot.core.enums;

import lombok.Getter;

@Getter
public enum Emoji {

    NONE(""),
    INFO("ℹ️"),
    WARN("⚠️"),
    ERROR("❌"),
    SUCCESS("✅");

    private final String emoji;

    Emoji(String emoji) {
        this.emoji = emoji;
    }
}