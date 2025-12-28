package org.bot.nullbot.service.impl;

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
    public void addGroup(Long groupId) {
        groupMapper.insert(new GroupPO(groupId, 2));
    }
}
