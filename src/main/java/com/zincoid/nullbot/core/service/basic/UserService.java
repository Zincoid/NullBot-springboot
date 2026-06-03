package com.zincoid.nullbot.core.service.basic;

import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.query.UserQuery;

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

    PageResult<UserPO> getPage(UserQuery query);

    void adds(List<UserPO> users);

    boolean delete(Long id);

    boolean update(UserPO user);
}
