package org.bot.nullbot.component.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.StompPrincipal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketListener
{
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if (user instanceof StompPrincipal stompUser) {
            // 构造发送的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", stompUser.getUserId());
            userInfo.put("userName", stompUser.getUserName());
            // 发送到用户私有队列 客户端需订阅 -> /user/queue/info
            messagingTemplate.convertAndSendToUser(
                    stompUser.getName(),
                    "/queue/info",
                    userInfo
            );
            log.info("▽ [WebSocketListener] 用户 {} 连接已建立 - SessionID: {}",
                    stompUser.getUserId(), accessor.getSessionId());
            return;
        }
        log.warn("▽ [WebSocketListener] 连接已建立但未获取到用户信息 - SessionID: {}",
                accessor.getSessionId());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("▽ [WebSocketListener] 连接已断开 - SessionID: {}", accessor.getSessionId());
    }
}
