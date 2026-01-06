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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Transactional
    public ItemPO getItem(Integer itemId) {
        return itemMapper.selectById(itemId);
    }

    @Override
    public ItemPO getRandomItem() {
        Rarity rarity = DrawUtil.drawRarityByProbability();
        List<ItemPO> itemList = itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getAvailable, true).eq(ItemPO::getRarity, rarity));
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    @Transactional
    public ItemPO getRandomHighValueItem() {
        Rarity rarity = Rarity.GOLD;
        List<ItemPO> itemList = itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getAvailable, true).ge(ItemPO::getRarity, rarity));
        return DrawUtil.drawItemByLogPrice(itemList);
    }

    @Override
    @Transactional
    public ItemPO getAndKeepRandomItem(Long userId) {
        if (userService.decreaseDrawTimes(userId)) {
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getAvailable, true).eq(ItemPO::getRarity, rarity));
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if(inventoryService.increaseInventory(userId, item.getId(), 1))
                return item;
            else
                return null;
        }else
            return null;
    }

    @Override
    @Transactional
    public boolean exist(Integer itemId) {
        return !itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, itemId)).isEmpty();
    }

    @Override
    @Transactional
    public boolean isUsable(Integer itemId) {
        return !itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, itemId).eq(ItemPO::getCategory, Category.USABLE)).isEmpty();
    }

    @Override
    @Transactional
    public String getCommandFromItemDesc(Integer itemId) {
        String description = itemMapper.selectList(new LambdaQueryWrapper<ItemPO>().eq(ItemPO::getId, itemId)).getFirst().getDescription();
        Matcher m = Pattern.compile("\\{(.*?)}").matcher(description);
        if (m.find())
            return m.group(1);
        else
            return null;
    }
}
