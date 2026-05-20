package com.zincoid.nullbot.websocket.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.entity.StompPrincipal;
import com.zincoid.nullbot.core.entity.result.WebResult;
import com.zincoid.nullbot.core.service.SystemService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SystemService systemService;

    @MessageMapping("/invoke")
    @SendToUser("/queue/invoke")
    public WebResult invoke(String command) {
        log.info("◉ [WebSocketController] 指令调用 - {}", command);
        try {
            String result = systemService.invoke(command);
            return WebResult.success("调用成功").withData("result", result);
        } catch (Exception e) {
            return WebResult.fail("调用失败").withData("result", e.toString());
        }
    }

    @MessageMapping("/info")
    @SendToUser("/queue/info")
    public Map<String, Object> info(Principal principal, SimpMessageHeaderAccessor accessor) {
        log.info("◉ [WebSocketController] 获取信息 - SessionID: {}", accessor.getSessionId());
        StompPrincipal stompUser = (StompPrincipal) principal;
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sessionId", accessor.getSessionId());
        userInfo.put("userId", stompUser.getUserId());
        userInfo.put("userName", stompUser.getUserName());
        return userInfo;
    }
}
