package com.zincoid.nullbot.core.enums.setting;

public enum ChatStrategy {

    DIRECT,
    EMBEDDING,
    TOOLS;

    public ChatStrategy next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
