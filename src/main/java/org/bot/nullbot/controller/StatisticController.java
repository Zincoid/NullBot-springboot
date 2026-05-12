package org.bot.nullbot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.entity.result.WebResult;
import org.bot.nullbot.entity.vo.StatisticVO;
import org.bot.nullbot.service.StatisticService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/nullbot/statistic")
@RestController
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping
    public WebResult Statistic(){
        StatisticVO statisticVO = statisticService.getStatistic();
        if(statisticVO != null){
            return WebResult.success().withMsg("获取成功")
                    .withData("totalVisits", statisticVO.getTotalVisits())
                    .withData("visitsXAxis", statisticVO.getVisitsXAxis())
                    .withData("visitsData", statisticVO.getVisitsData())
                    .withData("topGroupsAxis", statisticVO.getTopGroupsAxis())
                    .withData("topGroupsData", statisticVO.getTopGroupsData())
                    .withData("topUsersAxis", statisticVO.getTopUsersAxis())
                    .withData("topUsersData", statisticVO.getTopUsersData())
                    .withData("topCommandsAxis", statisticVO.getTopCommandsAxis())
                    .withData("topCommandsData", statisticVO.getTopCommandsData());
        }else{
            return WebResult.fail().withMsg("获取失败");
        }
    }
}
