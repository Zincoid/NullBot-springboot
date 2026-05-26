package com.zincoid.nullbot.bot.exception;

public class NullBotException extends RuntimeException {  // 捕获后发送控制台LOG和群提示消息
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
