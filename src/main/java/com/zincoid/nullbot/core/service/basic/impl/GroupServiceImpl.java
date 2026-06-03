package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.tool.BotOperator;
import com.zincoid.nullbot.core.model.data.query.GroupQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.mapper.GroupMapper;
import com.zincoid.nullbot.core.service.basic.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final BotOperator botOperator;
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
            Bot bot = botOperator.getBot();
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
    public PageResult<GroupPO> getPage(GroupQuery query) {
        return PageResult.of(groupMapper.selectPage(query.toPage(), null));
    }

    @Override
    public void adds(List<GroupPO> groups) {
        groupMapper.insert(groups);
    }

    @Override
    public boolean deleteById(Long id) {
        return groupMapper.deleteById(id) == 1;
    }

    @Override
    public boolean update(GroupPO group) {
        return groupMapper.updateById(group) == 1;
    }
}
