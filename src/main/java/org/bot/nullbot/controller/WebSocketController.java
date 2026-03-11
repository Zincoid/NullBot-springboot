package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SystemService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController
{
    private final SystemService systemService;

    @MessageMapping("/invoke")
    @SendToUser("/queue/invoke")
    public WebResult invoke(String command) {
        log.info("◉ [WebSocketController] 客户端调用 - {}", command);
        try {
            String result = systemService.invoke(command);
            return WebResult.success().addMsg("调用成功").addData("result", result);
        } catch (Exception e) {
            return WebResult.fail().addMsg("调用失败").addData("result", e.toString());
        }
    }
}
