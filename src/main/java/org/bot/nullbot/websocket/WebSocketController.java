package org.bot.nullbot.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class WebSocketController
{
    @MessageMapping("/command")  // 客户端发送到 /ws/command 的消息会由这个方法处理
    public void handleCommand(String command) {
        log.info("◉ [WebSocketController] 接收到客户端命令 - {}", command);
        // 处理命令...
    }
}
