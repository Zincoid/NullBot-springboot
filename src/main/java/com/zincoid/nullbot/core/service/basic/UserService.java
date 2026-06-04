package com.zincoid.nullbot.core.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.query.UserQuery;

public interface UserService extends IService<UserPO> {

    PageResult<UserPO> getPage(UserQuery query);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, int newAccess);

    void updateAllNames();

    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    int plusExperience(Long userId, int exp);
}
