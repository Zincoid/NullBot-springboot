package com.zincoid.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.entity.page.DataPage;
import com.zincoid.nullbot.mapper.InventoryMapper;
import com.zincoid.nullbot.mapper.ItemMapper;
import com.zincoid.nullbot.mapper.UserMapper;
import com.zincoid.nullbot.entity.po.ItemPO;
import com.zincoid.nullbot.enums.Category;
import com.zincoid.nullbot.enums.Rarity;
import com.zincoid.nullbot.util.DrawUtil;
import com.zincoid.nullbot.service.InventoryService;
import com.zincoid.nullbot.service.ItemService;
import com.zincoid.nullbot.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final InventoryService inventoryService;
    private final UserService userService;

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

    // =================== BOT功能相关 ===================

    @Override
    public ItemPO get(Integer id) {
        return itemMapper.selectById(id);
    }

    @Override
    public ItemPO getRandom() {
        Rarity rarity = DrawUtil.drawRarityByProbability();
        List<ItemPO> itemList = itemMapper.selectList(
                new LambdaQueryWrapper<ItemPO>()
                        .ne(ItemPO::getCategory, Category.BREAD)  // 排除的种类
                        .eq(ItemPO::getAvailable, true)
                        .eq(ItemPO::getRarity, rarity));
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    public ItemPO getRandomHighValue() {
        Rarity rarity = Rarity.GOLD;
        List<ItemPO> itemList = itemMapper.selectList(
                new LambdaQueryWrapper<ItemPO>()
                        .ne(ItemPO::getCategory, Category.BREAD)  // 排除的种类
                        .eq(ItemPO::getAvailable, true)
                        .ge(ItemPO::getRarity, rarity));
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    @Transactional
    public ItemPO drawAndKeepRandom(Long userId) {
        if (userService.decreaseDrawTimes(userId)) {
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper.selectList(
                    new LambdaQueryWrapper<ItemPO>()
                            .ne(ItemPO::getCategory, Category.BREAD)  // 排除的种类
                            .eq(ItemPO::getAvailable, true)
                            .eq(ItemPO::getRarity, rarity)
            );
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if(inventoryService.increase(userId, item.getId(), 1))
                return item;
            else
                return null;
        }else
            return null;
    }

    @Override
    public boolean exist(Integer id) {
        return !itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, id)).isEmpty();
    }

    @Override
    public boolean isUsable(Integer id) {
        return itemMapper.selectById(id).getCommand() != null;
    }

    @Override
    public String getCommand(Integer id) {
        return  itemMapper.selectById(id).getCommand();
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<ItemPO> getList() {
        return itemMapper.selectList(null);
    }

    @Override
    public DataPage<ItemPO> getPage(Integer current, Integer size) {
        Page<ItemPO> page = new Page<>(current, size);
        Page<ItemPO> itemPage;
        itemPage = itemMapper.selectPage(page, new LambdaQueryWrapper<ItemPO>().orderByAsc(ItemPO::getId));
        return new DataPage<>(itemPage.getRecords(), itemPage.getCurrent(), itemPage.getPages(), itemPage.getTotal(), itemPage.getSize());
    }

    @Override
    public boolean add(ItemPO item) {
        return itemMapper.insert(item) == 1;
    }

    @Override
    public void adds(List<ItemPO> items) { itemMapper.insert(items); }

    @Override
    public boolean update(ItemPO item) {
        return itemMapper.updateById(item) == 1;
    }

    @Override
    public boolean deleteById(Integer id) {
        return itemMapper.deleteById(id) == 1;
    }
}
