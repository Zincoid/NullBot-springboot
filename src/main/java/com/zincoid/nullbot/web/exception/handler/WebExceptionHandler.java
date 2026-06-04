package com.zincoid.nullbot.web.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.web.exception.CommonException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

    // 异步请求失效异常 (Oss服务)
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncRequestNotUsable(AsyncRequestNotUsableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof ClientAbortException) {
            log.warn("▽ [WebExceptionHandler] 客户端断开连接: {}", cause.getMessage());
        } else {
            log.error("▽ [WebExceptionHandler] 异步请求不可用: ", e);
        }
    }

    // 拦截所有其他异常
    @ExceptionHandler
    public WebResult<Void> handleException(Exception e) {
        log.error("▽ [WebExceptionHandler] 未知异常: ", e);
        return WebResult.fail("服务器运行出错: " + e.getMessage());
    }

    // 自定义服务器异常
    @ExceptionHandler(CommonException.class)
    public WebResult<Void> handleCommonException(CommonException e) {
        return WebResult.fail(e.getMessage());
    }

    // 实体参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public WebResult<Void> handleValidation(MethodArgumentNotValidException e) {
        return WebResult.fail(e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; ")));
    }

    // 简单参数校验异常
    @ExceptionHandler(ConstraintViolationException.class)
    public WebResult<Void> handleConstraintViolation(ConstraintViolationException e) {
        return WebResult.fail(e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; ")));
    }
}
