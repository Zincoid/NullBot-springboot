package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.service.SayingService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/saying")
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
            return WebResult.success().addMsg("获取成功").addData("saying", saying.toString());
        }else{
            return WebResult.fail().addMsg("获取失败");
        }
    }

    @GetMapping("/{currentPage}/{pageSize}")
    public WebResult getSayingByPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize){
        SayingPage sayingPage = sayingService.getSayingByPage(currentPage, pageSize);
        return WebResult.success().addMsg("查询成功").addData("sayingPage", sayingPage);
    }

    @DeleteMapping("/{id}")
    public WebResult deleteSaying(@PathVariable Integer id){
        if(sayingService.deleteById(id)){
            return WebResult.success().addMsg("删除成功");
        }else{
            return WebResult.fail().addMsg("删除失败");
        }
    }
}
