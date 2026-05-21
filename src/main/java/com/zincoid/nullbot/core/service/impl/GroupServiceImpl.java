package com.zincoid.nullbot.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.dto.page.DataPage;
import com.zincoid.nullbot.core.model.po.GroupPO;
import com.zincoid.nullbot.core.mapper.GroupMapper;
import com.zincoid.nullbot.core.service.GroupService;
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
    public GroupPO get(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public void add(Long id, String name) {
        groupMapper.insert(new GroupPO(id, name, 2));
    }

    @Override
    public void updateName(Long id, String newName) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, id)
                .set(GroupPO::getName, newName));
    }

    // =================== 限权功能相关 ===================

    @Override
    public boolean exist(Long id) {
        return groupMapper.selectById(id) != null;
    }

    @Override
    public int getAccess(Long id) {
        return groupMapper.selectById(id).getAccess();
    }

    @Override
    public void setAccess(Long id, Integer newAccess) {
        groupMapper.update(null, new LambdaUpdateWrapper<GroupPO>()
                .eq(GroupPO::getId, id)
                .set(GroupPO::getAccess, newAccess));
    }

    // =================== 数据库功能相关 ===================

    @Override
    @Transactional
    public void updateAllNames() {
        groupMapper.selectList(null).forEach(group -> {
            Bot bot = botContainer.robots.get(botId);
            group.setName(bot.getGroupInfo(group.getId(), true).getData().getGroupName());
            groupMapper.updateById(group);
        });
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<GroupPO> getList() {
        return groupMapper.selectList(null);
    }

    @Override
    public DataPage<GroupPO> getPage(Integer current, Integer size) {
        Page<GroupPO> page = new Page<>(current, size);
        Page<GroupPO> groupPage = groupMapper.selectPage(page, new LambdaQueryWrapper<GroupPO>().orderByAsc(GroupPO::getId));
        return new DataPage<>(groupPage.getRecords(), groupPage.getCurrent(), groupPage.getPages(), groupPage.getTotal(), groupPage.getSize());
    }

    @Override
    public void adds(List<GroupPO> groups) { groupMapper.insert(groups); }

    @Override
    public boolean deleteById(Long id) { return groupMapper.deleteById(id) == 1; }

    @Override
    public boolean update(GroupPO group) {
        return groupMapper.updateById(group) == 1;
    }
}
