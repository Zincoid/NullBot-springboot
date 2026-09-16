package com.zincoid.nullbot.web.interceptor;

import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.module.security.JwtTool;
import com.zincoid.nullbot.web.exception.UnauthorizedException;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.context.WebCtx;
import com.zincoid.nullbot.core.utils.WebUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebInterceptor implements HandlerInterceptor {

    private final JwtTool jwtTool;

    public boolean preHandle(
            HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull Object handler
    ) throws Exception {

        String uri = req.getRequestURI();
        String ip = WebUtil.getClientIpAddress();
        log.info("◎ [WebInterceptor] 来自 {} 的请求 - {} {}", ip, req.getMethod(), uri);

        if (uri.equals("/nullbot/auth/login") || uri.equals("/nullbot/auth/guest")) {
            log.info("└─[WebInterceptor] 登录放行");
            return true;
        }
        if (uri.equals("/nullbot/auth/regist")) {
            log.info("└─[WebInterceptor] 注册放行");
            return true;
        }
        if (uri.startsWith("/nullbot/oss")) {
            log.info("└─[WebInterceptor] OSS放行");
            return true;
        }

        String token = req.getHeader("token");
        JWT jwt;

        try {
            jwt = jwtTool.parseJwt(token);
        } catch (UnauthorizedException e) {
            log.info("└─[WebInterceptor] 验证失败");
            WebResult<Void> error = WebResult.fail(e.getMessage());
            res.getWriter().write(JSONObject.toJSONString(error));
            return false;
        }

        Long userId = jwtTool.getAs(jwt, "id", Long.class);
        Integer userType = jwtTool.getAs(jwt, "type", Integer.class);
        WebCtx.init(userId, userType);

        if (userType == 0) {
            log.info("└─[WebInterceptor] 访客放行");
            return true;
        }
        if (userType == 1) {
            log.info("└─[WebInterceptor] 管理放行");
            return true;
        }

        log.info("└─[WebInterceptor] 用户类型不存在");
        return false;
    }

    public void afterCompletion(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull Object handler,
            Exception ex
    ) {
        WebCtx.remove();
    }
}
