package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.page.InventoryPage;
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

    // =================== BOT功能相关 ===================

    @Override
    @Transactional
    public List<InventoryPO> getInventories(Long userId) {
        return inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).orderByDesc(InventoryPO::getRarity));
    }

    @Override
    @Transactional
    public InventoryPage getInventoriesPage(Long userId, long p, long size) {
        Page<InventoryPO> page = new Page<>(p, size);
        Page<InventoryPO> inventoryPage = inventoryMapper
                .selectPage(page, new LambdaQueryWrapper<InventoryPO>()
                .eq(InventoryPO::getOwnerId, userId)
                .orderByDesc(InventoryPO::getRarity)
                .orderByDesc(InventoryPO::getPrice)
                .orderByAsc(InventoryPO::getId));
        return new InventoryPage(inventoryPage.getRecords(), inventoryPage.getCurrent(), inventoryPage.getPages(), inventoryPage.getTotal(), inventoryPage.getSize());
    }

    @Override
    @Transactional
    public boolean increaseInventory(Long userId, Integer itemId, int i) {
        UserPO user = userMapper.selectById(userId);
        if(inventoryMapper.sumAmountByUserId(userId) >= user.getCapacity()) return false;
        ItemPO item = itemMapper.selectById(itemId);
        if(item == null) return false;
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if(inventories == null || inventories.isEmpty()){
            inventoryMapper.insert(new InventoryPO(null, userId, item.getId(), item.getName(), item.getRarity(), item.getPrice(), 1));
            return true;
        }else if(inventories.size() == 1){
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() + i);
            inventoryMapper.updateById(inventory);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean decreaseInventory(Long userId, Integer itemId, int i) {
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if(inventories == null || inventories.isEmpty()){
            return false;
        }else if(inventories.size() == 1 && inventories.getFirst().getAmount() >= i){
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() - i);
            if (inventory.getAmount() > 0)
                inventoryMapper.updateById(inventory);
            else
                inventoryMapper.deleteById(inventory.getId());
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean sellInventory(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if(item == null) return false;
        if(decreaseInventory(userId, itemId, i)){
            UserPO user = userMapper.selectById(userId);
            user.setCash(user.getCash() + item.getPrice() * i);
            userMapper.updateById(user);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean buyInventory(Long userId, Integer itemId, int i) {
        UserPO user = userMapper.selectById(userId);
        ItemPO item = itemMapper.selectById(itemId);
        if(item == null) return false;
        int totalPrice = item.getPrice() * i;
        if (user.getCash() >= totalPrice) {
            user.setCash(user.getCash() - totalPrice);
            userMapper.updateById(user);
            increaseInventory(userId, itemId, i);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean sellInventoryByRarity(Long userId, Rarity rarity) {
        List<InventoryPO> inventoriesByRarity = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getRarity, rarity));
        if(inventoriesByRarity == null || inventoriesByRarity.isEmpty()) return false;
        for(InventoryPO inventory : inventoriesByRarity){
            sellInventory(userId, inventory.getItemId(), inventory.getAmount());
        }
        return true;
    }

    @Override
    @Transactional
    public int getTotalAmountByUserId(Long userId) {
        return inventoryMapper.sumAmountByUserId(userId);
    }
}
