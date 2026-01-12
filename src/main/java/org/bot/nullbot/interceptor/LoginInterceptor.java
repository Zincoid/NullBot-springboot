package org.bot.nullbot.interceptor;

import com.alibaba.fastjson.JSONObject;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor
{
    private static final List<String> GUEST_FORBIDDEN_URLS = Arrays.asList(
            "/nullbot/file/init",
            "/nullbot/file/upload",
            "/nullbot/file/createDir",
            "/nullbot/file/delete",
            "/nullbot/file/rename",

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

    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        String url = req.getRequestURL().toString();
        log.info("[管理系统-JWT验证] URL - {}",url);

        if(url.contains("/nullbot/login")){
            log.info("[管理系统-JWT验证] 登录放行");
            return true;
        }
        if(url.contains("/nullbot/guest")){
            log.info("[管理系统-JWT验证] 访客放行");
            return true;
        }
        if(url.contains("/nullbot/preview")){
            log.info("[管理系统-JWT验证] 预览放行");
            return true;
        }

        String jwt = req.getHeader("token");
        if(!StringUtils.hasLength(jwt)){
            log.info("[管理系统-JWT验证] 令牌缺失");
            WebResult error = WebResult.fail().addMsg("Invalid Token");
            String info = JSONObject.toJSONString(error);
            res.getWriter().write(info);
            return false;
        }

        Claims claims;

        try {
            claims = JwtUtil.parseJWT(jwt);
        } catch (Exception e) {
            // e.printStackTrace();
            log.info("[管理系统-JWT验证] 解析失败");
            WebResult error = WebResult.fail().addMsg("Invalid Token");
            String info = JSONObject.toJSONString(error);
            res.getWriter().write(info);
            return false;
        }

        if(claims.get("type", Integer.class) == 0){
            for (String forbiddenUrl : GUEST_FORBIDDEN_URLS) {
                if(url.contains(forbiddenUrl)){
                    log.info("[管理系统-JWT验证] 访客禁止访问 - {}", forbiddenUrl);
                    WebResult error = WebResult.fail().addMsg("No Access");
                    String info = JSONObject.toJSONString(error);
                    res.getWriter().write(info);
                    return false;
                }
            }
            return true;
        }else if(claims.get("type", Integer.class) == 1){
            log.info("[管理系统-JWT验证] 管理员放行");
            return true;
        }

        log.info("[管理系统-JWT验证] 用户类型不存在");
        return false;
    }
}