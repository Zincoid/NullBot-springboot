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

    List<GroupPO> getList();

    DataPage<GroupPO> getPage(Integer current, Integer size);

    void adds(List<GroupPO> groups);

    boolean deleteById(Long id);

    boolean update(GroupPO group);
}
