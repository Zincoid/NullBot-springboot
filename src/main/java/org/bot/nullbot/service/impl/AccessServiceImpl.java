package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public boolean existGroup(long targetId) {
        return groupMapper.selectById(targetId) != null;
    }

    @Override
    public boolean existUser(long targetId) {
        return userMapper.selectById(targetId) != null;
    }

    @Override
    public int getGroupAccess(Long groupId) {
        return groupMapper.selectById(groupId).getAccess();
    }

    @Override
    public int getUserAccess(Long userId) {
        return userMapper.selectById(userId).getAccess();
    }

    @Override
    public void setGroupAccess(long groupId, int newAccess) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, groupId)
                .set(GroupPO::getAccess, newAccess));
    }

    @Override
    public void setUserAccess(long userId, int newAccess) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, userId)
                .set(UserPO::getAccess, newAccess));
    }
}
