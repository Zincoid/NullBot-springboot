package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.UserPO;

public interface UserService
{
    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    UserPO getUser(Long userId);

    void addUser(Long userId);

    boolean existUser(long targetId);

    int getUserAccess(Long userId);

    void setUserAccess(long userId, int newAccess);
}
