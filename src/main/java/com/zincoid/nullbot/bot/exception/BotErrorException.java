package com.zincoid.nullbot.bot.exception;

public class BotErrorException extends RuntimeException {

    public BotErrorException(String message) {
        super(message);
    }
    public BotErrorException(Throwable throwable) {
        super(throwable);
    }
    public BotErrorException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
