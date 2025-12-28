package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.mapper.GroupMapper;
import org.bot.nullbot.service.GroupService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService
{
    private final GroupMapper groupMapper;

    @Override
    public GroupPO getGroup(Long groupId) {
        return groupMapper.selectById(groupId);
    }

    @Override
    public void addGroup(Long groupId, String groupName) {
        groupMapper.insert(new GroupPO(groupId, groupName, 2));
    }

    @Override
    public void setGroupName(Long groupId, String groupName) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, groupId)
                .set(GroupPO::getName, groupName));
    }

    @Override
    public boolean existGroup(Long groupId) {
        return groupMapper.selectById(groupId) != null;
    }

    @Override
    public int getGroupAccess(Long groupId) {
        return groupMapper.selectById(groupId).getAccess();
    }

    @Override
    public void setGroupAccess(Long groupId, int newAccess) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, groupId)
                .set(GroupPO::getAccess, newAccess));
    }
}
