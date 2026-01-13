package org.bot.nullbot.exception;

public class NullBotMsgException extends RuntimeException  // 发送 控制台LOG和群提示消息
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
