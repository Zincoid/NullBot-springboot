package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
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
    public UserPO getUser(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void addUser(Long userId, String userName) {
        userMapper.insert(new UserPO(userId, userName, 0, 1, 0, 100, 50));
    }

    @Override
    public void setUserName(Long userId, String userName) {
        userMapper.update(null, new LambdaUpdateWrapper<UserPO>()
                .eq(UserPO::getId, userId)
                .set(UserPO::getName, userName));
    }

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

    @Override
    public void updateAllNames() {
        userMapper.selectList(null).forEach(user -> {
            Bot bot = botContainer.robots.get(botId);
            user.setName(bot.getStrangerInfo(user.getId(), true).getData().getNickname());
            userMapper.updateById(user);
        });
    }
}
