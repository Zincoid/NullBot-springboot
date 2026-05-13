package org.bot.nullbot.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler
    public WebResult handleException(Exception e) {  // 拦截所有异常
        log.error("▽ [WebExceptionHandler] ", e);
        return WebResult.fail("服务器运行出错: " + e.getMessage());
    }

    @ExceptionHandler
    public WebResult handleIllegalArgumentException(IllegalArgumentException e) {  // 部分方法抛出
        return WebResult.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public WebResult handleValidation(MethodArgumentNotValidException e) {  // 参数校验异常
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return WebResult.fail(message);
    }
}
