package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.SayingPage;
import org.bot.nullbot.entity.po.SayingPO;
import org.bot.nullbot.entity.result.WebResult;

import java.util.List;

public interface SayingService
{
    int insert(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    List<SayingPO> getList();

    SayingPO getRand();

    SayingPage getSayingByPage(Integer currentPage, Integer pageSize);
}
