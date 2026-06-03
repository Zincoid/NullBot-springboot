package com.zincoid.nullbot.core.service.basic;

import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.model.data.query.GroupQuery;

import java.util.List;

public interface GroupService {

    GroupPO get(Long id);

    void add(Long id, String name);

    void updateName(Long id, String newName);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, Integer newAccess);

    void updateAllNames();

    List<GroupPO> getList();

    PageResult<GroupPO> getPage(GroupQuery query);

    void adds(List<GroupPO> groups);

    boolean deleteById(Long id);

    boolean update(GroupPO group);
}
