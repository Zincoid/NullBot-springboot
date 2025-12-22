package org.bot.nullbot.service;

import org.bot.nullbot.entity.vo.StatisticVO;

public interface StatisticService
{
    void increaseOnDate();

    void increase(Long groupId, Long userId, String userName, String command);

    StatisticVO getStatistic();
}
