package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.GroupPO;

public interface GroupService
{
    GroupPO getGroup(Long groupId);

    void addGroup(Long groupId);

    boolean existGroup(Long groupId);

    int getGroupAccess(Long groupId);

    void setGroupAccess(Long groupId, int newAccess);
}
