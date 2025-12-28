package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.mapper.GroupMapper;
import org.bot.nullbot.service.GroupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService
{
    // 数据库更新用
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

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

    @Override
    public void updateAllNames() {
        groupMapper.selectList(null).forEach(group -> {
            Bot bot = botContainer.robots.get(botId);
            group.setName(bot.getGroupInfo(group.getId(), true).getData().getGroupName());
            groupMapper.updateById(group);
        });
    }
}
