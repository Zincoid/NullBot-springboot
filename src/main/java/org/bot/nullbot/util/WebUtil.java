package org.bot.nullbot.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class WebUtil
{
    /** 获取 request 对象 **/
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes)requestAttributes).getRequest();
    }

    /** 获取 response 对象 **/
    public static HttpServletResponse getResponse() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes)requestAttributes).getResponse();
    }

    /** 获取 token **/
    public static String getToken() {
        return getRequest().getHeader("token");
    }

    /** 获取 登录用户 ID **/
    @Deprecated
    public static Long getLoginId() {
        Claims token = JwtUtil.parseJwt(getRequest().getHeader("token"));
        return token.get("id", Long.class);
    }

    /** 获取 登录用户 Type **/
    @Deprecated
    public static Integer getLoginType() {
        Claims token = JwtUtil.parseJwt(getRequest().getHeader("token"));
        return token.get("type", Integer.class);
    }

    /** 获取 客户端 IP 地址 **/
    public static String getClientIpAddress() {
        HttpServletRequest request = getRequest();
        String[] ipHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能有多个IP 取第一个
                if ("X-Forwarded-For".equalsIgnoreCase(header)) {
                    int index = ip.indexOf(',');
                    if (index != -1) {
                        ip = ip.substring(0, index);
                    }
                }
                return ip;
            }
        }

        // 如果从请求头中都没有获取到则使用 getRemoteAddr()
        return request.getRemoteAddr();
    }
}
