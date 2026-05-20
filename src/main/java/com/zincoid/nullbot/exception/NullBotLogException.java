package com.zincoid.nullbot.exception;

public class NullBotLogException extends RuntimeException {  // 捕获后发送控制台LOG
    public NullBotLogException(String message) { super(message); }
    public NullBotLogException(Throwable throwable) {
        super(throwable);
    }
    public NullBotLogException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
