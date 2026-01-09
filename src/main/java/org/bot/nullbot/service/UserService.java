package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.UserPage;
import org.bot.nullbot.entity.po.UserPO;

import java.util.List;

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

    List<UserPO> getUserList();

    UserPage getUserByPage(Integer currentPage, Integer pageSize);

    void addUsers(List<UserPO> users);

    boolean deleteById(Integer id);

    boolean updateUser(UserPO user);
}
