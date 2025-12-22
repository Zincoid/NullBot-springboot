package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.vo.StatisticVO;
import org.bot.nullbot.service.StatisticService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/nullbot/statistic")
@RequiredArgsConstructor
@Slf4j
public class StatisticController
{
    private final StatisticService statisticService;

    @GetMapping
    public WebResult Statistic(){
        StatisticVO statisticVO = statisticService.getStatistic();
        if(statisticVO != null){
            return WebResult.success().addMsg("获取成功").addData("statistic", statisticVO.toString());
        }else{
            return WebResult.fail().addMsg("获取失败");
        }
    }
}
