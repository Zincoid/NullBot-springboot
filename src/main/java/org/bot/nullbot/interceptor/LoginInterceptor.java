package org.bot.nullbot.interceptor;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String url = req.getRequestURL().toString();
        log.info("[管理系统-JWT验证] URL - {}",url);

        if(url.contains("/nullbot/login")){
            log.info("[管理系统-JWT验证] 登录放行");
            return true;
        }

        if(url.contains("/nullbot/preview")){
            log.info("[管理系统-JWT验证] 预览放行");
            return true;
        }

        String jwt = req.getHeader("token");
        if(!StringUtils.hasLength(jwt)){
            log.info("[管理系统-JWT验证] 令牌缺失");
            WebResult error = WebResult.fail().addMsg("Not Login");
            String notLogin = JSONObject.toJSONString(error);
            res.getWriter().write(notLogin);
            return false;
        }

        try {
            JwtUtil.parseJWT(jwt);
        } catch (Exception e) {
            // e.printStackTrace();
            log.info("[管理系统-JWT验证] 解析失败");
            WebResult error = WebResult.fail().addMsg("Not Login");
            String notLogin = JSONObject.toJSONString(error);
            res.getWriter().write(notLogin);
            return false;
        }

        log.info("[管理系统-JWT验证] 合法放行");
        return true;
    }
}