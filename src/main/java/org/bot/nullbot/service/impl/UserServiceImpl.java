package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dao.mapper.InventoryMapper;
import org.bot.nullbot.dao.mapper.ItemMapper;
import org.bot.nullbot.dao.mapper.UserMapper;
import org.bot.nullbot.dao.po.InventoryPO;
import org.bot.nullbot.dao.po.UserPO;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService
{
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public boolean decreaseDrawTimes(Long userId) {
        UserPO user = userMapper.selectById(userId);
        if(user == null){
            userMapper.insert(new UserPO(userId, 1, 49, 100));
            return true;
        }else if(user.getDrawTimes() > 0){
            user.setDrawTimes(user.getDrawTimes() - 1);
            userMapper.updateById(user);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public List<InventoryPO> getInventories(Long userId) {
        return inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).orderByDesc(InventoryPO::getRarity));
    }
}
