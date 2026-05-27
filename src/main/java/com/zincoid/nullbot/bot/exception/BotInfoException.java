package com.zincoid.nullbot.bot.exception;

import com.zincoid.nullbot.core.enums.Emoji;

public class BotInfoException extends RuntimeException {

    private final Emoji emoji;

    public BotInfoException(Emoji emoji, String message) {
        super(message);
        this.emoji = emoji;
    }
    public BotInfoException(String message) {
        super(message);
        this.emoji = Emoji.NONE;
    }
    public BotInfoException(Throwable throwable) {
        super(throwable);
        this.emoji = Emoji.NONE;
    }
    public BotInfoException(String message, Throwable throwable) {
        super(message, throwable);
        this.emoji = Emoji.NONE;
    }

    @Override
    public String getMessage() {
        return emoji.getEmoji() + super.getMessage();
    }
}
