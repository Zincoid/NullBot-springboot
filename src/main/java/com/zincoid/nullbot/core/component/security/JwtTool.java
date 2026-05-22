package com.zincoid.nullbot.core.component.security;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.zincoid.nullbot.web.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool {

    private final JWTSigner jwtSigner;
    private final ObjectMapper objectMapper;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
        this.objectMapper = new ObjectMapper();
    }

    // 创建Jwt
    public String createJwt(Long id, Integer type, Duration ttl) {
        return JWT.create()
                .setPayload("id", id)
                .setPayload("type", type)
                .setExpiresAt(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .setSigner(jwtSigner)
                .sign();
    }

    // 解析Jwt
    public JWT parseJwt(String token) {
        if (!StringUtils.hasLength(token))
            throw new UnauthorizedException("No Token");
        JWT jwt;
        try {
            jwt = JWT.of(token).setSigner(jwtSigner);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid Token", e);
        }
        if (!jwt.verify())
            throw new UnauthorizedException("Invalid Token");
        try {
            JWTValidator.of(jwt).validateDate();
        } catch (ValidateException e) {
            throw new UnauthorizedException("Expired Token");
        }
        return jwt;
    }

    // 获取Data
    public <T> T getAs(JWT jwt, String attr, Class<T> clazz) {
        Object payload = jwt.getPayload(attr);
        if (payload == null) return null;
        try {
            if (clazz.isInstance(payload))
                return clazz.cast(payload);
            return objectMapper.convertValue(payload, clazz);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid Attr: " + e.getMessage());
        }
    }
}
