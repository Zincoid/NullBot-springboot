package com.zincoid.nullbot.core.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.query.UserQuery;
import com.zincoid.nullbot.core.model.data.dto.UserDTO;

public interface UserService extends IService<UserPO> {

    void update(UserDTO user);

    void delete(Long id);

    PageResult<UserPO> page(UserQuery query);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, int newAccess);

    void updateAllNames();

    void increaseDrawTimes(Long userId, int i);

    boolean decreaseDrawTimes(Long userId);

    int plusExperience(Long userId, int exp);
}
