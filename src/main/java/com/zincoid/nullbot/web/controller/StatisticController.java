package com.zincoid.nullbot.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.entity.result.WebResult;
import com.zincoid.nullbot.core.entity.vo.StatisticVO;
import com.zincoid.nullbot.core.service.StatisticService;
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
    public WebResult Statistic() {
        StatisticVO statisticVO = statisticService.getStatistic();
        if (statisticVO != null) {
            return WebResult.success("获取成功").withData("statistic", statisticVO);
        } else {
            return WebResult.fail("获取失败");
        }
    }
}
