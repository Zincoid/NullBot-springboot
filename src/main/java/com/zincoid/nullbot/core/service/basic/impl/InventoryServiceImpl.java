package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zincoid.nullbot.core.service.basic.ItemService;
import com.zincoid.nullbot.core.service.basic.UserService;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.mapper.InventoryMapper;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, InventoryPO> implements InventoryService {

    private final UserService userService;
    private final ItemService itemService;

    @Override
    public List<InventoryVO> listVO(Long userId) {
        return baseMapper.selectVOList(userId);
    }

    @Override
    public PageResult<InventoryVO> pageVO(Long userId, Integer current, Integer size) {
        Page<InventoryVO> page = Page.of(current, size);
        Page<InventoryVO> inventoryVOPage = baseMapper.selectVOPage(page, userId);
        return PageResult.of(inventoryVOPage);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean add(Long userId, Integer itemId, int i) {
        ItemPO item = itemService.getById(itemId);
        if (item == null) return false;
        UserPO user = userService.getById(userId);
        if (getTotalAmount(userId) >= user.getCapacity()) return false;
        List<InventoryPO> inventories = lambdaQuery().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId).list();
        if (inventories == null || inventories.isEmpty()) {
            return save(new InventoryPO(null, userId, item.getId(), i));
        } else if (inventories.size() == 1) {
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() + i);
            return updateById(inventory);
        } else
            return false;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean remove(Long userId, Integer itemId, int i) {
        List<InventoryPO> inventories = lambdaQuery().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId).list();
        if (inventories == null || inventories.size() != 1) return false;
        InventoryPO inventory = inventories.getFirst();
        if (inventory.getAmount() < i) return false;
        inventory.setAmount(inventory.getAmount() - i);
        if (inventory.getAmount() > 0)
            return updateById(inventory);
        else
            return removeById(inventory);
    }

    @Override
    @Transactional
    public boolean sell(Long userId, Integer itemId, int i) {
        ItemPO item = itemService.getById(itemId);
        if (item == null) return false;
        if (remove(userId, itemId, i)) {
            UserPO user = userService.getById(userId);
            user.setCash(user.getCash() + item.getPrice() * i);
            return userService.updateById(user);
        } else
            return false;
    }

    @Override
    @Transactional
    public boolean sell(Long userId, Rarity rarity) {
        List<InventoryVO> InventoryVOS = listVO(userId);
        List<InventoryVO> inventoryVOSByRarity = InventoryVOS.stream()
                .filter(inventoryVO -> inventoryVO.getRarity() == rarity)
                .toList();
        if (inventoryVOSByRarity.isEmpty()) return false;
        for (InventoryVO inventoryVO : inventoryVOSByRarity) {
            sell(userId, inventoryVO.getItemId(), inventoryVO.getAmount());
        }
        return true;
    }

    @Override
    @Transactional
    public boolean buy(Long userId, Integer itemId, int i) {
        ItemPO item = itemService.getById(itemId);
        if (item == null) return false;
        UserPO user = userService.getById(userId);
        int totalPrice = item.getPrice() * i;
        if (user.getCash() >= totalPrice) {
            user.setCash(user.getCash() - totalPrice);
            return userService.updateById(user) && add(userId, itemId, i);
        } else
            return false;
    }

    @Override
    @Transactional
    public ItemPO draw(Long userId) {
        if (userService.decreaseDrawTimes(userId)) {
            ItemPO item = itemService.getRandom();
            if (item != null && add(userId, item.getId(), 1))
                return item;
        }
        return null;
    }

    @Override
    public int getTotalAmount(Long userId) {
        return baseMapper.getTotalAmountByUserId(userId);
    }
}
