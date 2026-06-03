package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.mapper.InventoryMapper;
import com.zincoid.nullbot.core.mapper.ItemMapper;
import com.zincoid.nullbot.core.mapper.UserMapper;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

    // =================== BOT功能相关 ===================

    @Override
    public PageResult<InventoryVO> getVOPage(Long userId, Integer current, Integer size) {
        Page<InventoryVO> page = Page.of(current, size);
        Page<InventoryVO> inventoryVOPage = inventoryMapper.selectVOPage(page, userId);
        return PageResult.of(inventoryVOPage);
    }

    @Override
    public int getTotalAmount(Long userId) {
        return inventoryMapper.getTotalAmountByUserId(userId);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean increase(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if (item == null) return false;
        UserPO user = userMapper.selectById(userId);
        if (getTotalAmount(userId) >= user.getCapacity()) return false;
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if (inventories == null || inventories.isEmpty()) {
            return inventoryMapper.insert(new InventoryPO(null, userId, item.getId(), i)) == 1;
        } else if (inventories.size() == 1) {
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() + i);
            return inventoryMapper.updateById(inventory) == 1;
        } else
            return false;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean decrease(Long userId, Integer itemId, int i) {
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if (inventories == null || inventories.size() != 1) return false;
        InventoryPO inventory = inventories.getFirst();
        if (inventory.getAmount() < i) return false;
        inventory.setAmount(inventory.getAmount() - i);
        if (inventory.getAmount() > 0)
            return inventoryMapper.updateById(inventory) == 1;
        else
            return inventoryMapper.deleteById(inventory) == 1;
    }

    @Override
    @Transactional
    public boolean sell(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if (item == null) return false;
        if (decrease(userId, itemId, i)) {
            UserPO user = userMapper.selectById(userId);
            user.setCash(user.getCash() + item.getPrice() * i);
            return userMapper.updateById(user) == 1;
        } else
            return false;
    }

    @Override
    @Transactional
    public boolean buy(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if (item == null) return false;
        UserPO user = userMapper.selectById(userId);
        int totalPrice = item.getPrice() * i;
        if (user.getCash() >= totalPrice) {
            user.setCash(user.getCash() - totalPrice);
            return userMapper.updateById(user) == 1 && increase(userId, itemId, i);
        } else
            return false;
    }

    @Override
    @Transactional
    public boolean sellByRarity(Long userId, Rarity rarity) {
        List<InventoryVO> InventoryVOS = getVOList(userId);
        List<InventoryVO> inventoryVOSByRarity = InventoryVOS.stream()
                .filter(inventoryVO -> inventoryVO.getRarity() == rarity)
                .toList();
        if (inventoryVOSByRarity.isEmpty()) return false;
        for (InventoryVO inventoryVO : inventoryVOSByRarity) {
            sell(userId, inventoryVO.getItemId(), inventoryVO.getAmount());
        }
        return true;
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<InventoryVO> getVOList(Long userId) {
        return inventoryMapper.selectVOList(userId);
    }

    @Override
    public List<InventoryPO> getList() {
        return inventoryMapper.selectList(null);
    }

    @Override
    public void adds(List<InventoryPO> inventories) {
        inventoryMapper.insert(inventories);
    }

    @Override
    public boolean deleteById(Integer id) {
        return inventoryMapper.deleteById(id) == 1;
    }

    @Override
    public boolean update(InventoryPO inventory) {
        return inventoryMapper.updateById(inventory) == 1;
    }
}
