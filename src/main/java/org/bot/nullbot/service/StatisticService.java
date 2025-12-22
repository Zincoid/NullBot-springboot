package org.bot.nullbot.service;

public interface StatisticService
{
    void increaseOnDate();

    void increase(Long groupId, Long userId, String userName, String command);
}
