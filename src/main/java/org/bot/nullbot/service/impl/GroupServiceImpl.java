package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.GroupPO;
import org.bot.nullbot.mapper.GroupMapper;
import org.bot.nullbot.service.GroupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    // 数据库更新用
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final GroupMapper groupMapper;

    // =================== 注册功能相关 ===================

    @Override
    public GroupPO getGroup(Long groupId) {
        return groupMapper.selectById(groupId);
    }

    @Override
    public void addGroup(Long groupId, String groupName) {
        groupMapper.insert(new GroupPO(groupId, groupName, 2));
    }

    @Override
    public void updateGroupName(Long groupId, String groupName) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, groupId)
                .set(GroupPO::getName, groupName));
    }

    // =================== 限权功能相关 ===================

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

    // =================== 数据库功能相关 ===================

    @Override
    @Transactional
    public void updateAllGroupNames() {
        groupMapper.selectList(null).forEach(group -> {
            Bot bot = botContainer.robots.get(botId);
            group.setName(bot.getGroupInfo(group.getId(), true).getData().getGroupName());
            groupMapper.updateById(group);
        });
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<GroupPO> getGroupList() {
        return groupMapper.selectList(null);
    }

    @Override
    public DataPage<GroupPO> getGroupByPage(Integer currentPage, Integer pageSize) {
        Page<GroupPO> page = new Page<>(currentPage, pageSize);
        Page<GroupPO> groupPage = groupMapper.selectPage(page, new LambdaQueryWrapper<GroupPO>().orderByAsc(GroupPO::getId));
        return new DataPage<>(groupPage.getRecords(), groupPage.getCurrent(), groupPage.getPages(), groupPage.getTotal(), groupPage.getSize());
    }

    @Override
    public void addGroups(List<GroupPO> groups) { groupMapper.insert(groups); }

    @Override
    public boolean deleteById(Long groupId) { return groupMapper.deleteById(groupId) == 1; }

    @Override
    public boolean updateGroup(GroupPO group) {
        return groupMapper.updateById(group) == 1;
    }
}
