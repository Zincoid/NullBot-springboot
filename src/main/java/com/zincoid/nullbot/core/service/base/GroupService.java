package com.zincoid.nullbot.core.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.data.query.GroupQuery;

public interface GroupService extends IService<GroupPO> {

    PageResult<GroupPO> page(GroupQuery query);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, Integer newAccess);

    void updateAllNames();
}
