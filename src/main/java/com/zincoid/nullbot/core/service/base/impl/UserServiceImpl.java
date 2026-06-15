package com.zincoid.nullbot.core.service.base.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.model.data.query.UserQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.UserMapper;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.service.base.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {

    private final BotOperator botOperator;

    @Override
    public PageResult<UserPO> page(UserQuery query) {
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
    public void setAccess(Long id, int newAccess) {
        lambdaUpdate().eq(UserPO::getId, id).set(UserPO::getAccess, newAccess).update();
    }

    @Override
    @Transactional
    public void updateAllNames() {
        list().forEach(user -> {
            Bot bot = botOperator.getBot();
            user.setName(bot.getStrangerInfo(user.getId(), true).getData().getNickname());
            updateById(user);
        });
    }

    @Override
    @Transactional
    public void increaseDrawTimes(Long userId, int i) {
        UserPO user = getById(userId);
        user.setDrawTimes(user.getDrawTimes() + i);
        updateById(user);
    }

    @Override
    @Transactional
    public boolean decreaseDrawTimes(Long userId) {
        UserPO user = getById(userId);
        if (user.getDrawTimes() > 0) {
            user.setDrawTimes(user.getDrawTimes() - 1);
            updateById(user);
            return true;
        } else
            return false;
    }

    @Override
    @Transactional
    public int plusExperience(Long userId, int exp) {
        UserPO user = getById(userId);
        int upgrade = user.plusExperience(exp);
        updateById(user);
        return upgrade;
    }
}
