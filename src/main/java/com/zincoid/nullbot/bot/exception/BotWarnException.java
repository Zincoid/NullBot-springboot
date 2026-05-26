package com.zincoid.nullbot.bot.exception;

public class BotWarnException extends RuntimeException {

    public BotWarnException(String message) {
        super(message);
    }
    public BotWarnException(Throwable throwable) {
        super(throwable);
    }
    public BotWarnException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
