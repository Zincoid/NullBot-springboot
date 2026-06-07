package com.zincoid.nullbot.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.model.result.WebResult;
import com.zincoid.nullbot.core.model.data.vo.StatsVO;
import com.zincoid.nullbot.core.service.system.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/nullbot/stats")
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public WebResult<StatsVO> stats() {
        StatsVO statsVO = statsService.getStatsVO();
        if (statsVO != null) {
            return WebResult.success("获取成功", statsVO);
        } else {
            return WebResult.fail("获取失败");
        }
    }
}
