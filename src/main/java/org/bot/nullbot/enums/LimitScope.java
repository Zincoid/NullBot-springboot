package org.bot.nullbot.enums;

public enum LimitScope {

    User,
    Group,
    Cmd;

    public LimitScope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
