package com.zincoid.nullbot.core.component.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
@Deprecated
public class WebSocketHandler extends TextWebSocketHandler {

    // 线程安全的会话集合
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("▽ [WebSocketHandler] 新连接加入 - 当前连接数: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("▽ [WebSocketHandler] 接收到消息 - {}", message.getPayload());
        // 处理客户端发送的消息 暂时忽略
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("▽ [WebSocketHandler] 有连接关闭 - 当前连接数：{}", sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("▽ [WebSocketHandler] 传输错误 - {}", exception.getMessage());
        sessions.remove(session);
    }

    /**
     * 广播日志至所有连接客户端
     * @param level 日志等级
     * @param message 日志内容 (可用 Json 格式)
     */
    public static void broadcast(String level, String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    String json = "{\"level\":\"" + level + "\",\"message\":\"" + message + "\"}";
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
