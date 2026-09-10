package com.zincoid.nullbot.core.enums.setting;

public enum LimitScope {

    USER,
    GROUP,
    CMD;

    public LimitScope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
