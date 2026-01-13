package org.bot.nullbot.exception;

public class NullBotMsgException extends RuntimeException
{
    public NullBotMsgException(String message) {
        super(message);
    }

    public NullBotMsgException(Throwable throwable) {
        super(throwable);
    }

    public NullBotMsgException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
