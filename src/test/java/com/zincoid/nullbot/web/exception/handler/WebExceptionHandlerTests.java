package com.zincoid.nullbot.web.exception.handler;

import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.web.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WebExceptionHandlerTests {

    private final WebExceptionHandler handler = new WebExceptionHandler();

    @Test
    void handleUnauthorized() {
        WebResult<?> res = handler.handleUnauthorized(
                new UnauthorizedException("登录已过期"));
        assertEquals(0, res.getCode());
        assertEquals("登录已过期", res.getMessage());
    }
}
