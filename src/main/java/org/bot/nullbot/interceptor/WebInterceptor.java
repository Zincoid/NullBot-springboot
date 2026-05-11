package org.bot.nullbot.interceptor;

import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.entity.result.WebResult;
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
            "/nullbot/system",

            "/nullbot/delete",
            "/nullbot/update",
            "/nullbot/changePwd",

            "/nullbot/file/init",
            "/nullbot/file/upload",
            "/nullbot/file/createDir",
            "/nullbot/file/delete",
            "/nullbot/file/rename",
            "/nullbot/file/move",

            "/nullbot/saying/delete",
            "/nullbot/saying/exportCsv",
            "/nullbot/saying/importCsv",

            "/nullbot/group/delete",
            "/nullbot/group/update",
            "/nullbot/group/exportCsv",
            "/nullbot/group/importCsv",

            "/nullbot/setting",

            "/nullbot/user/delete",
            "/nullbot/user/update",
            "/nullbot/user/exportCsv",
            "/nullbot/user/importCsv",

            "/nullbot/item/add",
            "/nullbot/item/delete",
            "/nullbot/item/update",
            "/nullbot/item/exportCsv",
            "/nullbot/item/importCsv",

            "/nullbot/inventory/add",
            "/nullbot/inventory/delete",
            "/nullbot/inventory/update",
            "/nullbot/inventory/exportCsv",
            "/nullbot/inventory/importCsv"
        );
    }

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
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
            WebResult error = WebResult.fail().addMsg(e.getMessage());
            res.getWriter().write(JSONObject.toJSONString(error));
            return false;
        }

        Integer userType = jwtTool.getAs(jwt, "type", Integer.class);

        if (userType == 0) {
            for (String forbiddenUrl : GUEST_FORBIDDEN_URLS) {
                if (url.contains(forbiddenUrl)) {
                    log.info("└─[WebInterceptor] 访客受限");
                    WebResult error = WebResult.fail().addMsg("No Access");
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
}