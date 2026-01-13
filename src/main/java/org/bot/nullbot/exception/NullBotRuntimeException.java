package org.bot.nullbot.exception;

public class NullBotRuntimeException extends RuntimeException
{
    public NullBotRuntimeException(String message) {
        super(message);
    }

    public NullBotRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public NullBotRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
