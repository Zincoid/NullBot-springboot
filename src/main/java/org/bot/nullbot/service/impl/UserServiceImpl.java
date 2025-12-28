package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
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
    public void addUser(Long userId) {
        userMapper.insert(new UserPO(userId, 0, 1, 0, 100, 50));
    }
}
