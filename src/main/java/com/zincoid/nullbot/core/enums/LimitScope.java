package com.zincoid.nullbot.core.enums;

public enum LimitScope {

    User,
    Group,
    Cmd;

    public LimitScope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
