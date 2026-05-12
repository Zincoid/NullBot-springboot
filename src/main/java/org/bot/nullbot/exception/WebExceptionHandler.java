package org.bot.nullbot.exception;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler
    public WebResult handleException(Exception e) {
        log.error("▽ [WebExceptionHandler] ", e);
        return WebResult.fail("服务器运行出错: " + e.getMessage());
    }
}
