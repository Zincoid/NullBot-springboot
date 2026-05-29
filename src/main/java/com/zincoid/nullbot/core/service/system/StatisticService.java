package com.zincoid.nullbot.core.service.system;

import com.zincoid.nullbot.core.model.data.vo.StatisticVO;

public interface StatisticService {

    void increaseOnDate();

    void increase(Long groupId, Long userId, String userName, String command);

    StatisticVO getStatistic();

    Long getUses(Long userId);
}
