package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.WebResult;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot")
@RequiredArgsConstructor
@Slf4j
public class SayingController
{
    private final SayingService sayingService;

    @GetMapping("/randomSaying")
    public WebResult RandomSaying(){
        log.info("[管理系统] 获取随机语录");
        SayingPO saying = sayingService.getRand();
        if(saying != null){
            return WebResult.success().addMsg("获取成功.").addData("saying", saying.toString());
        }else{
            return WebResult.fail().addMsg("获取失败.");
        }
    }
}
