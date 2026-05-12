package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.UserPO;

import java.util.List;

public interface UserService {

    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    int plusExperience(Long userId, int exp);

    UserPO get(Long id);

    void add(Long id, String name);

    void updateName(Long id, String newName);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, int newAccess);

    void updateAllNames();

    List<UserPO> getList();

    DataPage<UserPO> getPage(Integer current, Integer size);

    void adds(List<UserPO> users);

    boolean delete(Integer id);

    boolean update(UserPO user);
}
