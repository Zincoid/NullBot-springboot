package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.entity.vo.InventoryVO;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

    // =================== BOT功能相关 ===================

    @Override
    public DataPage<InventoryVO> getVOPage(Long userId, Integer current, Integer size) {
        Page<InventoryPO> page = new Page<>(current, size);
        Page<InventoryPO> inventoryPOPage = inventoryMapper.selectPage(
                page, new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId)
        );
        List<InventoryVO> inventoryVOS = inventoryPOPage.getRecords().stream()
                .map(inventoryPO -> {
                    ItemPO item = itemMapper.selectById(inventoryPO.getItemId());
                    return new InventoryVO(
                            inventoryPO.getId(),
                            inventoryPO.getOwnerId(),
                            inventoryPO.getItemId(),
                            item.getName(),
                            item.getCategory(),
                            item.getRarity(),
                            item.getPrice(),
                            inventoryPO.getAmount()
                    );
                })
                .sorted(Comparator
                        .comparing(InventoryVO::getRarity, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getPrice, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getId)
                )
                .toList();
        return new DataPage<>(inventoryVOS, inventoryPOPage.getCurrent(),
                inventoryPOPage.getPages(), inventoryPOPage.getTotal(), inventoryPOPage.getSize());
    }

    @Override
    public int getTotalAmount(Long userId) {
        return inventoryMapper.sumAmountByUserId(userId);
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean increase(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if(item == null) return false;
        UserPO user = userMapper.selectById(userId);
        if(inventoryMapper.sumAmountByUserId(userId) >= user.getCapacity()) return false;
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if(inventories == null || inventories.isEmpty()){
            return inventoryMapper.insert(new InventoryPO(null, userId, item.getId(), i)) == 1;
        }else if(inventories.size() == 1){
            InventoryPO inventory = inventories.getFirst();
            inventory.setAmount(inventory.getAmount() + i);
            return inventoryMapper.updateById(inventory) == 1;
        }else
            return false;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public boolean decrease(Long userId, Integer itemId, int i) {
        List<InventoryPO> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId).eq(InventoryPO::getItemId, itemId));
        if(inventories == null || inventories.size() != 1) return false;
        InventoryPO inventory = inventories.getFirst();
        if(inventory.getAmount() < i) return false;
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
        if(item == null) return false;
        if(decrease(userId, itemId, i)){
            UserPO user = userMapper.selectById(userId);
            user.setCash(user.getCash() + item.getPrice() * i);
            return userMapper.updateById(user) == 1;
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean buy(Long userId, Integer itemId, int i) {
        ItemPO item = itemMapper.selectById(itemId);
        if(item == null) return false;
        UserPO user = userMapper.selectById(userId);
        int totalPrice = item.getPrice() * i;
        if (user.getCash() >= totalPrice) {
            user.setCash(user.getCash() - totalPrice);
            return userMapper.updateById(user) == 1 && increase(userId, itemId, i);
        }else
            return false;
    }

    @Override
    @Transactional
    public boolean sellByRarity(Long userId, Rarity rarity) {
        List<InventoryVO> InventoryVOS = getVOList(userId);
        List<InventoryVO> inventoryVOSByRarity = InventoryVOS.stream()
                .filter(inventoryVO -> inventoryVO.getRarity() == rarity)
                .toList();
        if(inventoryVOSByRarity.isEmpty()) return false;
        for(InventoryVO inventoryVO : inventoryVOSByRarity){
            sell(userId, inventoryVO.getItemId(), inventoryVO.getAmount());
        }
        return true;
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<InventoryVO> getVOList(Long userId) {
        List<InventoryPO> inventoryPOS = inventoryMapper.selectList(
                new LambdaQueryWrapper<InventoryPO>().eq(InventoryPO::getOwnerId, userId)
        );
        return inventoryPOS.stream()
                .map(inventoryPO -> {
                    ItemPO item = itemMapper.selectById(inventoryPO.getItemId());
                    return new InventoryVO(
                            inventoryPO.getId(),
                            inventoryPO.getOwnerId(),
                            inventoryPO.getItemId(),
                            item.getName(),
                            item.getCategory(),
                            item.getRarity(),
                            item.getPrice(),
                            inventoryPO.getAmount()
                    );
                })
                .sorted(Comparator
                        .comparing(InventoryVO::getRarity, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getPrice, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getId)
                )
                .toList();
    }

    @Override
    public List<InventoryPO> getAll() { return inventoryMapper.selectList(null); }

    @Override
    public void add(List<InventoryPO> inventories) { inventoryMapper.insert(inventories); }

    @Override
    public boolean delete(Integer id) { return inventoryMapper.deleteById(id) == 1; }

    @Override
    public boolean update(InventoryPO inventory) { return inventoryMapper.updateById(inventory) == 1; }
}
