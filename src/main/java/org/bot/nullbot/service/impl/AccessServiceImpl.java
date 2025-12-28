package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.mapper.GroupMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.service.AccessService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService
{
    private final GroupMapper groupMapper;
    private final UserMapper userMapper;

    @Override
    public int getGroupAccess(Long groupId) {
        return groupMapper.selectById(groupId).getAccess();
    }

    @Override
    public int getUserAccess(Long userId) {
        return userMapper.selectById(userId).getAccess();
    }

    @Override
    public boolean setGroupAccess(long groupId, int newAccess) {
        GroupPO groupPO = groupMapper.selectById(groupId);
        if(groupPO == null) {
            return false;
        } else {
            groupPO.setAccess(newAccess);
            groupMapper.updateById(groupPO);
            return true;
        }
    }

    @Override
    public boolean setUserAccess(long userId, int newAccess) {
        UserPO userPO = userMapper.selectById(userId);
        if(userPO == null) {
            return false;
        } else {
            userPO.setAccess(newAccess);
            userMapper.updateById(userPO);
            return true;
        }
    }
}
