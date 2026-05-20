package com.zincoid.nullbot.service;

import com.zincoid.nullbot.entity.vo.StatisticVO;

public interface StatisticService {

    void increaseOnDate();

    void increase(Long groupId, Long userId, String userName, String command);

    StatisticVO getStatistic();
}
