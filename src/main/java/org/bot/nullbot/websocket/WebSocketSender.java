package org.bot.nullbot.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketSender
{
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 广播日志消息 - /topic/log
     * @param level  日志级别
     * @param message 日志内容
     */
    public void broadcast(String level, String message) {
        String json = String.format("{\"level\":\"%s\",\"message\":\"%s\"}", level, message);
        messagingTemplate.convertAndSend("/topic/log", json);
    }
}
