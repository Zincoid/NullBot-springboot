package org.bot.nullbot.exception;

public class NullBotLogException extends RuntimeException
{
    public NullBotLogException(String message) {
        super(message);
    }

    public NullBotLogException(Throwable throwable) {
        super(throwable);
    }

    public NullBotLogException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
