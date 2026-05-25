package com.zincoid.nullbot.core.component.ai.chat.enums;

public enum ChatStrategy {

    DIRECT,
    EMBEDDING,
    TOOLS;

    public ChatStrategy next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
