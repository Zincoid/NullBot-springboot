package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 数据库更新用
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

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
        if(user.getDrawTimes() > 0){
            user.setDrawTimes(user.getDrawTimes() - 1);
            userMapper.updateById(user);
            return true;
        }else
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
            Bot bot = botContainer.robots.get(botId);
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
    public DataPage<UserPO> getPage(Integer current, Integer size) {
        Page<UserPO> page = new Page<>(current, size);
        Page<UserPO> userPage = userMapper.selectPage(page, new LambdaQueryWrapper<UserPO>().orderByAsc(UserPO::getId));
        return new DataPage<>(userPage.getRecords(), userPage.getCurrent(), userPage.getPages(), userPage.getTotal(), userPage.getSize());
    }

    @Override
    public void adds(List<UserPO> users) { userMapper.insert(users); }

    @Override
    public boolean delete(Integer id) { return userMapper.deleteById(id) == 1; }

    @Override
    public boolean update(UserPO user) {
        return userMapper.updateById(user) == 1;
    }
}
