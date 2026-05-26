package com.zincoid.nullbot.bot.exception;

public class BotOmitException extends RuntimeException {

    public BotOmitException(String message) {
        super(message);
    }
    public BotOmitException(Throwable throwable) {
        super(throwable);
    }
    public BotOmitException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
