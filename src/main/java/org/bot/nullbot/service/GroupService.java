package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.GroupPO;

public interface GroupService
{
    GroupPO getGroup(Long groupId);

    void addGroup(Long groupId);
}
