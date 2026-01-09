package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.entity.po.SayingPO;

import java.util.List;

public interface SayingService
{
    int insert(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    SayingPO getRand();

    SayingPO getRandByUserId(Long userId);

    List<SayingPO> getSayingList();

    SayingPage getSayingByPage(Integer currentPage, Integer pageSize);

    void addSayings(List<SayingPO> sayings);
}
