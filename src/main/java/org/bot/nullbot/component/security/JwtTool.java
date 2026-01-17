package org.bot.nullbot.component.security;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.bot.nullbot.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool
{
    private final JWTSigner jwtSigner;
    private final ObjectMapper objectMapper;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 创建 Token
     * @param id 用户ID
     * @param type 用户类型
     * @return Token
     */
    public String createJwt(Long id, Integer type, Duration ttl) {
        return JWT.create()
                .setPayload("id", id)
                .setPayload("type", type)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    /**
     * 解析 token
     * @param token token
     * @return JWT对象
     */
    public JWT parseJwt(String token) {
        // 校验Token非空
        if (!StringUtils.hasLength(token))
            throw new UnauthorizedException("No Token");

        JWT jwt;

        // 设置JWT对象
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Token", e);
        }

        // 校验是否有效
        if (!jwt.verify())
            throw new UnauthorizedException("Invalid Token");

        // 校验是否过期
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            throw new UnauthorizedException("Expired Token");
        }

        return jwt;
    }


    /**
     * 解析 JWT 获取数据
     * @param jwt JWT对象
     * @param attr 属性名
     * @param clazz 类型
     * @return Jwt中的指定数据
     */
    public <T> T getAs(JWT jwt, String attr, Class<T> clazz) {
        Object payload = jwt.getPayload(attr);
        if (payload == null)
            throw new UnauthorizedException("No Info");
        try {
            if (clazz.isInstance(payload))
                return clazz.cast(payload);
            return objectMapper.convertValue(payload, clazz);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid Info: " + e.getMessage());
        }
    }

    /**
     * 解析 token 获取 用户ID
     * @param token token
     * @return 用户ID
     */
    public Long getLoginId(String token) {
        JWT jwt = parseJwt(token);
        return getAs(jwt, "id", Long.class);
    }

    /**
     * 解析 token 获取 用户Type
     * @param token token
     * @return 用户Type
     */
    public Integer getLoginType(String token) {
        JWT jwt = parseJwt(token);
        return getAs(jwt, "type", Integer.class);
    }
}
