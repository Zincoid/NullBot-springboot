package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.enums.Category;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.util.DrawUtil;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.ItemService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService
{
    private final InventoryService inventoryService;
    private final UserService userService;

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final InventoryMapper inventoryMapper;

    // =================== BOT功能相关 ===================

    @Override
    public ItemPO getItem(Integer itemId) {
        return itemMapper.selectById(itemId);
    }

    @Override
    public ItemPO getRandomItem() {
        Rarity rarity = DrawUtil.drawRarityByProbability();
        List<ItemPO> itemList = itemMapper.selectList(
                new LambdaQueryWrapper<ItemPO>()
                        .ne(ItemPO::getCategory, Category.BREAD)  // 排除的种类
                        .eq(ItemPO::getAvailable, true)
                        .eq(ItemPO::getRarity, rarity));
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    public ItemPO getRandomHighValueItem() {
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
    public ItemPO drawAndKeepRandomItem(Long userId) {
        if (userService.decreaseDrawTimes(userId)) {
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper.selectList(
                    new LambdaQueryWrapper<ItemPO>()
                            .ne(ItemPO::getCategory, Category.BREAD)  // 排除的种类
                            .eq(ItemPO::getAvailable, true)
                            .eq(ItemPO::getRarity, rarity)
            );
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if(inventoryService.increaseInventory(userId, item.getId(), 1))
                return item;
            else
                return null;
        }else
            return null;
    }

    @Override
    public boolean exist(Integer itemId) {
        return !itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, itemId)).isEmpty();
    }

    @Override
    public boolean isUsable(Integer itemId) {
        return itemMapper.selectById(itemId).getCommand() != null;
    }

    @Override
    public String getItemCommand(Integer itemId) {
        return  itemMapper.selectById(itemId).getCommand();
    }

    // =================== WEB功能相关 ===================

    @Override
    public List<ItemPO> getItemList() {
        return itemMapper.selectList(null);
    }

    // @Override
    // public ItemPage getItemByPage(Integer currentPage, Integer pageSize, Category category) {
    //     Page<ItemPO> page = new Page<>(currentPage, pageSize);
    //     Page<ItemPO> itemPage;
    //     if (category != null) {
    //         itemPage = itemMapper.selectPage(page, new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getCategory, category).orderByAsc(ItemPO::getId));
    //     }else{
    //         itemPage = itemMapper.selectPage(page, new LambdaQueryWrapper<ItemPO>().orderByAsc(ItemPO::getId));
    //     }
    //     return new ItemPage(itemPage.getRecords(), itemPage.getCurrent(), itemPage.getPages(), itemPage.getTotal(), itemPage.getSize());
    // }

    @Override
    public boolean updateItem(ItemPO item) {
        return itemMapper.updateById(item) == 1;
    }
}
