package org.bot.nullbot.websocket;

import cn.hutool.jwt.JWT;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.bot.nullbot.component.security.JwtTool;
import org.bot.nullbot.entity.StompPrincipal;
import org.bot.nullbot.entity.po.AdminPO;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.AdminService;
import org.bot.nullbot.service.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements ChannelInterceptor
{
    private final JwtTool jwtTool;
    private final AdminService adminService;
    private final UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String ip = (String) accessor.getSessionAttributes().get("clientIp");
            log.info("◎ [WebSocketManagement] 来自 {} 的连接请求", ip);
            // 从 Header 中获取 Token (例如 token: Bearer <token>)
            String authHeader = accessor.getFirstNativeHeader("token");
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer "))
                throw new IllegalArgumentException("No Token");
            String token = authHeader.substring(7);
            // 验证 Token
            if (validateToken(token)) {
                Long userId = extractUserId(token);
                AdminPO admin = adminService.info(userId);
                if (admin == null) {
                    log.info("└─[WebSocketInterceptor] 管理员不存在");
                    throw new IllegalArgumentException("Invalid Admin");
                }
                StompPrincipal user = new StompPrincipal(userId, admin.getUsername());
                // 将 Principal 设置到 StompHeaderAccessor 中
                // 后续可以在 @MessageMapping 方法中通过 Principal 参数获取
                accessor.setUser(user);
                log.info("└─[WebSocketInterceptor] 连接放行 - UserId: {}", userId);
                return message;
            } else {
                log.info("└─[WebSocketInterceptor] 验证失败");
                throw new IllegalArgumentException("Invalid Token");
            }
        }
        log.info("└─[WebSocketInterceptor] 默认放行");
        return message;
    }

    private boolean validateToken(String token) {
        try {
            jwtTool.parseJwt(token);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Long extractUserId(String token) {
        return jwtTool.getLoginId(token);
    }
}
