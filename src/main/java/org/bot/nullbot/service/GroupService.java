package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.GroupPO;

import java.util.List;

public interface GroupService {

    GroupPO get(Long id);

    void add(Long id, String name);

    void updateName(Long id, String newName);

    boolean exist(Long id);

    int getAccess(Long id);

    void setAccess(Long id, Integer newAccess);

    void updateAllNames();

    List<GroupPO> getAll();

    DataPage<GroupPO> getPage(Integer currentPage, Integer pageSize);

    void adds(List<GroupPO> groups);

    boolean delete(Long groupId);

    boolean update(GroupPO group);
}
