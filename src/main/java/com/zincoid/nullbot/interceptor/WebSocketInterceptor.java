package com.zincoid.nullbot.interceptor;

import cn.hutool.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.component.security.JwtTool;
import com.zincoid.nullbot.entity.StompPrincipal;
import com.zincoid.nullbot.entity.po.AdminPO;
import com.zincoid.nullbot.service.AdminService;
import com.zincoid.nullbot.util.WebCtxUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;

@Slf4j
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    private final AdminService adminService;
    private final JwtTool jwtTool;
    private final MessageChannel clientOutboundChannel;  // 注入出站通道

    public WebSocketInterceptor(
            @Lazy AdminService adminService,
            JwtTool jwtTool,
            @Lazy @Qualifier("clientOutboundChannel") MessageChannel clientOutboundChannel
    ) {
        this.adminService = adminService;
        this.jwtTool = jwtTool;
        this.clientOutboundChannel = clientOutboundChannel;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String ip = (String) accessor.getSessionAttributes().get("clientIp");
            log.info("◎ [WebSocketInterceptor] 来自 {} 的连接请求", ip);
            try {
                String authHeader = accessor.getFirstNativeHeader("token");
                if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer "))
                    throw new IllegalArgumentException("No Token");
                String token = authHeader.substring(7);
                JWT jwt;
                try {
                    jwt = jwtTool.parseJwt(token);
                } catch (Exception e) {
                    log.info("├─[WebSocketInterceptor] 验证失败");
                    throw new IllegalArgumentException("Invalid Token");
                }
                Long userId = jwtTool.getAs(jwt, "id", Long.class);
                Integer userType = jwtTool.getAs(jwt, "type", Integer.class);
                WebCtxUtil.set(userId, userType);  // 存储此次用户信息
                AdminPO admin = adminService.info(userId);
                if (admin == null) {
                    log.info("├─[WebSocketInterceptor] 管理员不存在");
                    throw new IllegalArgumentException("Invalid Admin");
                }
                StompPrincipal user = new StompPrincipal(userId, admin.getUsername());
                accessor.setUser(user);
                log.info("└─[WebSocketInterceptor] 连接放行 - UserID: {}", userId);
                return message;
            } catch (IllegalArgumentException e) {
                // 发送错误帧给客户端
                sendErrorMessage(accessor.getSessionId(), e.getMessage());
                return null;  // 阻止原消息继续传递
            }
        }
        return message;
    }

    private void sendErrorMessage(String sessionId, String errorMessage) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setSessionId(sessionId);
        errorAccessor.setMessage(errorMessage);  // 设置错误描述
        errorAccessor.setLeaveMutable(true);
        Message<byte[]> errorMessageObj = MessageBuilder.createMessage(new byte[0], errorAccessor.getMessageHeaders());
        clientOutboundChannel.send(errorMessageObj);
        log.error("└─[WebSocketInterceptor] 已向客户端 {} 发送错误帧 - {}", sessionId, errorMessage);
    }
}
