package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.UserPO;

public interface UserService
{
    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    UserPO getUser(Long userId);

    void addUser(Long userId, String userName);

    void setUserName(Long userId, String userName);

    boolean existUser(Long userId);

    int getUserAccess(Long userId);

    void setUserAccess(Long userId, int newAccess);
}
