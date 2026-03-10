package org.bot.nullbot.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class WebSocketLogger extends TextWebSocketHandler
{
    // 线程安全的会话集合
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("▽ [WebSocketLogger] 新连接加入 - 当前连接数: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 可以处理客户端发送的消息 暂时忽略
        log.info("▽ [WebSocketLogger] 接收到消息 - {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("▽ [WebSocketLogger] 有连接关闭 - 当前连接数：{}", sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("▽ [WebSocketLogger] 传输错误 - {}", exception.getMessage());
        sessions.remove(session);
    }

    /**
     * 向所有连接的客户端广播日志消息
     * @param log 日志内容（可以是 JSON 字符串）
     */
    public static void broadcast(String log) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(log));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
