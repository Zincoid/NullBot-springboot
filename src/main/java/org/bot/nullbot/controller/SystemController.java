package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.service.SystemService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
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
            List<String> params = List.of(command.split(" "));
            String beanName = params.get(0);
            String methodName = params.get(1);
            Object[] args = new Object[0];
            if (params.size() > 2) args = params.subList(2, params.size()).toArray();
            String result = systemService.invoke(beanName, methodName, args);
            return WebResult.success().addMsg("调用成功").addData("result", result);
        } catch (Exception e) {
            return WebResult.fail().addMsg("调用失败: " + e.getMessage());
        }
    }
}
