package org.bot.nullbot.enums;

public enum ChatScope
{
    Group,
    Personal,
    Monitor;

    public ChatScope next() {
        int nextOrdinal = (this.ordinal() + 1) % values().length;
        return values()[nextOrdinal];
    }
}
