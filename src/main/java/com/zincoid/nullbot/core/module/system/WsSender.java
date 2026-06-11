package com.zincoid.nullbot.core.module.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsSender {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 广播日志消息 - 目标频道 /topic/log
     * @param level  日志级别
     * @param message 日志内容
     */
    public void broadcast(String level, String message) {
        String json = String.format("{\"level\":\"%s\",\"message\":\"%s\"}", level, message);
        messagingTemplate.convertAndSend("/topic/log", json);
    }
}
