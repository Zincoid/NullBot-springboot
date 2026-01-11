package org.bot.nullbot.enums;

public enum Scope
{
    Group,
    Personal,
    Monitor;

    public Scope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
