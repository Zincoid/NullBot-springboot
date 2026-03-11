package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SystemService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nullbot/system")
@RequiredArgsConstructor
@Slf4j
public class SystemController
{
    private final SystemService systemService;

    @GetMapping("/invoke")
    public WebResult invoke(@RequestParam(defaultValue = "") String command){
        try {
            String result = systemService.invoke(command);
            return WebResult.success().addMsg("调用成功").addData("result", result);
        } catch (Exception e) {
            return WebResult.fail().addMsg("调用失败").addData("result", e.toString());
        }
    }
}
