package com.zincoid.nullbot.core.service.basic.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.mapper.ItemMapper;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.enums.Category;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.util.DrawUtil;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.ItemService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final InventoryService inventoryService;
    private final UserService userService;

    private final ItemMapper itemMapper;

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
        return itemMapper.exists(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, id));
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
    public PageResult<ItemPO> getPage(ItemQuery query) {
        return PageResult.of(itemMapper.selectPage(query.toPage(), null));
    }

    @Override
    public boolean add(ItemPO item) {
        return itemMapper.insert(item) == 1;
    }

    @Override
    public void adds(List<ItemPO> items) {
        itemMapper.insert(items);
    }

    @Override
    public boolean update(ItemPO item) {
        return itemMapper.updateById(item) == 1;
    }

    @Override
    public boolean deleteById(Integer id) {
        return itemMapper.deleteById(id) == 1;
    }
}
