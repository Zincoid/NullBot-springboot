package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.core.component.tool.BotOperator;
import com.zincoid.nullbot.core.model.data.query.UserQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.UserMapper;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final BotOperator botOperator;
    private final UserMapper userMapper;

    // =================== BOT功能相关 ===================

    @Override
    @Transactional
    public void increaseDrawTimes(Long userId, int i) {
        UserPO user = userMapper.selectById(userId);
        user.setDrawTimes(user.getDrawTimes() + i);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public boolean decreaseDrawTimes(Long userId) {
        UserPO user = userMapper.selectById(userId);
        if (user.getDrawTimes() > 0) {
            user.setDrawTimes(user.getDrawTimes() - 1);
            userMapper.updateById(user);
            return true;
        } else
            return false;
    }

    @Override
    @Transactional
    public int plusExperience(Long userId, int exp) {
        UserPO user = userMapper.selectById(userId);
        int upgrade = user.plusExperience(exp);
        userMapper.updateById(user);
        return upgrade;
    }

    // =================== 注册功能相关 ===================

    @Override
    public UserPO get(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public void add(Long id, String name) {
        userMapper.insert(new UserPO(id, name, 0, 1, 0, 0, 100, 50));
    }

    @Override
    public void updateName(Long id, String newName) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, id)
                .set(UserPO::getName, newName));
    }

    // =================== 限权功能相关 ===================

    @Override
    public boolean exist(Long id) {
        return userMapper.selectById(id) != null;
    }

    @Override
    public int getAccess(Long id) {
        return userMapper.selectById(id).getAccess();
    }

    @Override
    public void setAccess(Long id, int newAccess) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, id)
                .set(UserPO::getAccess, newAccess));
    }

    // =================== 数据库功能相关 ===================

    @Override
    @Transactional
    public void updateAllNames() {
        userMapper.selectList(null).forEach(user -> {
            Bot bot = botOperator.getBot();
            user.setName(bot.getStrangerInfo(user.getId(), true).getData().getNickname());
            userMapper.updateById(user);
        });
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<UserPO> getList() {
        return userMapper.selectList(null);
    }


    @Override
    public PageResult<UserPO> getPage(UserQuery query) {
        return PageResult.of(userMapper.selectPage(query.toPage(), null));
    }

    @Override
    public void adds(List<UserPO> users) {
        userMapper.insert(users);
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.deleteById(id) == 1;
    }

    @Override
    public boolean update(UserPO user) {
        return userMapper.updateById(user) == 1;
    }
}
