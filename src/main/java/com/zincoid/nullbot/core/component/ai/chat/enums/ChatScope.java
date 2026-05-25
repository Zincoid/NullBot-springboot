package com.zincoid.nullbot.core.component.ai.chat.enums;

public enum ChatScope {

    GROUP,
    PERSONAL,
    MONITOR;

    public ChatScope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
