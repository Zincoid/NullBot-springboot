package org.bot.nullbot.interceptor;

import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.util.UserCtxUtil;
import org.bot.nullbot.util.WebUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebInterceptor implements HandlerInterceptor {

    private final JwtTool jwtTool;
    private static final List<String> GUEST_FORBIDDEN_URLS;

    static {
        GUEST_FORBIDDEN_URLS = Arrays.asList(

                // 禁用系统和设置功能
                "/system",
                "/setting",

                // 禁止操作 Csv
                "/exportCsv",
                "/importCsv",

                // 禁止增删改
                "/add",
                "/delete",
                "/update",

                // 禁止修改密码
                "/changePwd",

                // 禁用部分文件功能
                "/file/init",
                "/file/upload",
                "/file/createDir",
                "/file/rename",
                "/file/move",
                "/file/setVisible"
        );
    }

    public boolean preHandle(
            HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull Object handler
    ) throws Exception {

        String url = req.getRequestURL().toString();
        String ip = WebUtil.getClientIpAddress();
        log.info("◎ [WebInterceptor] 来自 {} 的请求 - {}", ip, url);

        if (url.contains("/nullbot/login") || url.contains("/nullbot/guest")) {
            log.info("└─[WebInterceptor] 登录放行");
            return true;
        }
        if (url.contains("/nullbot/regist")) {
            log.info("└─[WebInterceptor] 注册放行");
            return true;
        }
        if (url.contains("/nullbot/preview")) {
            log.info("└─[WebInterceptor] 预览放行");
            return true;
        }

        String token = req.getHeader("token");
        JWT jwt;

        try {
            jwt = jwtTool.parseJwt(token);
        } catch (Exception e) {
            log.info("└─[WebInterceptor] 验证失败");
            WebResult error = WebResult.fail(e.getMessage());
            res.getWriter().write(JSONObject.toJSONString(error));
            return false;
        }

        Long userId = jwtTool.getAs(jwt, "id", Long.class);
        Integer userType = jwtTool.getAs(jwt, "type", Integer.class);

        UserCtxUtil.set(userId, userType);  // 存储此次用户信息

        if (userType == 0) {
            for (String forbiddenUrl : GUEST_FORBIDDEN_URLS) {
                if (url.contains(forbiddenUrl)) {
                    log.info("└─[WebInterceptor] 访客受限");
                    WebResult error = WebResult.fail("No Access");
                    res.getWriter().write(JSONObject.toJSONString(error));
                    return false;
                }
            }
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
        UserCtxUtil.remove();
    }
}
