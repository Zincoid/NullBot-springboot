package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.SayingPO;

import java.util.List;

public interface SayingService
{
    int insert(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    List<SayingPO> getList();

    SayingPO getRand();
}
