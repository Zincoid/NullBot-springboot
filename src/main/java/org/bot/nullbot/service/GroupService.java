package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.GroupPage;
import org.bot.nullbot.entity.po.GroupPO;

import java.util.List;

public interface GroupService {

    GroupPO getGroup(Long groupId);

    void addGroup(Long groupId, String groupName);

    void updateGroupName(Long groupId, String groupName);

    boolean existGroup(Long groupId);

    int getGroupAccess(Long groupId);

    void setGroupAccess(Long groupId, int newAccess);

    void updateAllGroupNames();

    List<GroupPO> getGroupList();

    GroupPage getGroupByPage(Integer currentPage, Integer pageSize);

    void addGroups(List<GroupPO> groups);

    boolean deleteById(Long groupId);

    boolean updateGroup(GroupPO group);
}
