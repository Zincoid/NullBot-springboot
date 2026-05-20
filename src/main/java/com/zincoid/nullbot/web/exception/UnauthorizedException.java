package com.zincoid.nullbot.web.exception;

public class UnauthorizedException extends RuntimeException {  // Web 鉴权异常
    public UnauthorizedException(String message) {
        super(message);
    }
    public UnauthorizedException(String message, Throwable throwable) {
        super(message, throwable);
    }
    public UnauthorizedException(Throwable throwable) {
        super(throwable);
    }
}
