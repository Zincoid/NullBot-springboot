package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.UserPage;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    // 数据库更新用
    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

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
    public UserPO getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void addUser(Long userId, String userName) {
        userMapper.insert(new UserPO(userId, userName, 0, 1, 0, 0, 100, 50));
    }

    @Override
    public void updateUserName(Long userId, String userName) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, userId)
                .set(UserPO::getName, userName));
    }

    // =================== 限权功能相关 ===================

    @Override
    public boolean existUser(Long userId) {
        return userMapper.selectById(userId) != null;
    }

    @Override
    public int getUserAccess(Long userId) {
        return userMapper.selectById(userId).getAccess();
    }

    @Override
    public void setUserAccess(Long userId, int newAccess) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, userId)
                .set(UserPO::getAccess, newAccess));
    }

    // =================== 数据库功能相关 ===================

    @Override
    @Transactional
    public void updateAllUserNames() {
        userMapper.selectList(null).forEach(user -> {
            Bot bot = botContainer.robots.get(botId);
            user.setName(bot.getStrangerInfo(user.getId(), true).getData().getNickname());
            userMapper.updateById(user);
        });
    }

    // =================== WEB功能相关 ===================

    @Override
    public UserPage getUserByPage(Integer currentPage, Integer pageSize) {
        Page<UserPO> page = new Page<>(currentPage, pageSize);
        Page<UserPO> userPage = userMapper.selectPage(page, new LambdaQueryWrapper<UserPO>().orderByAsc(UserPO::getId));
        return new UserPage(userPage.getRecords(), userPage.getCurrent(), userPage.getPages(), userPage.getTotal(), userPage.getSize());
    }

    @Override
    public boolean updateUser(UserPO user) {
        return userMapper.updateById(user) == 1;
    }
}
