package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.model.data.query.GroupQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.GroupPO;
import com.zincoid.nullbot.core.mapper.GroupMapper;
import com.zincoid.nullbot.core.service.basic.GroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupPO> implements GroupService {

    private final BotOperator botOperator;

    @Override
    public PageResult<GroupPO> page(GroupQuery query) {
        return PageResult.of(page(query.toPage(), null));
    }

    @Override
    public boolean exist(Long id) {
        return getById(id) != null;
    }

    @Override
    public int getAccess(Long id) {
        return getById(id).getAccess();
    }

    @Override
    public void setAccess(Long id, Integer newAccess) {
        lambdaUpdate().eq(GroupPO::getId, id).set(GroupPO::getAccess, newAccess).update();
    }

    @Override
    @Transactional
    public void updateAllNames() {
        list().forEach(group -> {
            Bot bot = botOperator.getBot();
            group.setName(bot.getGroupInfo(group.getId(), true).getData().getGroupName());
            updateById(group);
        });
    }
}
