package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.vo.StatisticVO;

public interface StatisticService {

    void increaseOnDate();

    void increase(Long groupId, Long userId, String userName, String command);

    StatisticVO getStatistic();
}
