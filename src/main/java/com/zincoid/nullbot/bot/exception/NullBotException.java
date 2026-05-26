package com.zincoid.nullbot.bot.exception;

public class NullBotException extends RuntimeException {

    public NullBotException(String message) {
        super(message);
    }
    public NullBotException(Throwable throwable) {
        super(throwable);
    }
    public NullBotException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
