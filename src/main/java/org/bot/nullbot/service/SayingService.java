package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.SayingPO;

import java.util.List;

public interface SayingService {

    int addSaying(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    SayingPO getRand();

    SayingPO getRandByUserId(Long userId);

    List<SayingPO> getSayingList();

    DataPage<SayingPO> getSayingByPage(Integer currentPage, Integer pageSize);

    void addSayings(List<SayingPO> sayings);
}
