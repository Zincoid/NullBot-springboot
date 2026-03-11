package org.bot.nullbot.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SystemService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.List;

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
            List<String> params = List.of(command.split(" "));
            if(params.size() < 2)
                return WebResult.fail().addMsg("调用失败").addData("result", "Not enough args...");
            String beanName = params.get(0);
            String methodName = params.get(1);
            Object[] args = new Object[0];
            if (params.size() > 2) args = params.subList(2, params.size()).toArray();
            String result = systemService.invoke(beanName, methodName, args);
            return WebResult.success().addMsg("调用成功").addData("result", result);
        } catch (Exception e) {
            return WebResult.fail().addMsg("调用失败").addData("result", e.toString());
        }
    }
}
