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
    public static HttpServletRequest getRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes)requestAttributes).getRequest();
    }

    /** 获取 response 对象 **/
    public static HttpServletResponse getResponse(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null){
            return null;
        }
        return ((ServletRequestAttributes)requestAttributes).getResponse();
    }

    /** 获取 token **/
    public static String getToken(){
        return getRequest().getHeader("token");
    }

    /** 获取 登录用户 ID **/
    @Deprecated
    public static Long getLoginId(){
        Claims token = JwtUtil.parseJWT(getRequest().getHeader("token"));
        return token.get("id", Long.class);
    }

    /** 获取 登录用户 Type **/
    @Deprecated
    public static Integer getLoginType(){
        Claims token = JwtUtil.parseJWT(getRequest().getHeader("token"));
        return token.get("type", Integer.class);
    }
}
