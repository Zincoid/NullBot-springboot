package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dao.mapper.InventoryMapper;
import org.bot.nullbot.dao.mapper.ItemMapper;
import org.bot.nullbot.dao.mapper.UserMapper;
import org.bot.nullbot.dao.po.InventoryPO;
import org.bot.nullbot.dao.po.ItemPO;
import org.bot.nullbot.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService
{
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;


    @Override
    @Transactional
    public boolean increaseInventory(Long userId, ItemPO item) {
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, item.getId()));
        if(inventories == null || inventories.isEmpty()){
            inventoryMapper.insert(new InventoryPO(null, userId, item.getId(), item.getName(), item.getRarity(), item.getPrice(), 1));
            return true;
        }else if(inventories.size() == 1){
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() + 1);
            inventoryMapper.updateById(inventory);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean decreaseInventory(Long userId, Integer itemId) {
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if(inventories == null || inventories.isEmpty()){
            return false;
        }else if(inventories.size() == 1){
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() - 1);
            if (inventory.getAmount() > 0)
                inventoryMapper.updateById(inventory);
            else
                inventoryMapper.deleteById(inventory.getId());
            return true;
        }else
            return false;
    }
}
