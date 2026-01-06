package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.UserPage;
import org.bot.nullbot.entity.po.UserPO;

public interface UserService
{
    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    int plusExperience(Long userId, int exp);

    UserPO getUser(Long userId);

    void addUser(Long userId, String userName);

    void updateUserName(Long userId, String userName);

    boolean existUser(Long userId);

    int getUserAccess(Long userId);

    void setUserAccess(Long userId, int newAccess);

    void updateAllUserNames();

    UserPage getUserByPage(Integer currentPage, Integer pageSize);

    boolean updateUser(UserPO user);
}
