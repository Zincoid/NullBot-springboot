package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SystemService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/nullbot/system")
@RestController
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    @GetMapping("/invoke")
    public WebResult invoke(@RequestParam(defaultValue = "") String command){
        try {
            String result = systemService.invoke(command);
            return WebResult.success().withMsg("调用成功").withData("result", result);
        } catch (Exception e) {
            return WebResult.fail().withMsg("调用失败").withData("result", e.toString());
        }
    }
}
