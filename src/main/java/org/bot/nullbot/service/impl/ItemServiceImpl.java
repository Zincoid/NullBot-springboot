package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.dao.mapper.InventoryMapper;
import org.bot.nullbot.dao.mapper.ItemMapper;
import org.bot.nullbot.dao.mapper.UserMapper;
import org.bot.nullbot.dao.po.ItemPO;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.plugin.util.game.DrawUtil;
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


    @Override
    @Transactional
    public ItemPO getAndKeepRandomItem(Long userId) {
        if (userService.decreaseDrawTimes(userId)) {
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper.selectList(new QueryWrapper<ItemPO>().eq("rarity", rarity.getValue()));
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if(inventoryService.increaseInventory(userId, item))
                return item;
            else
                return null;
        }else
            return null;
    }
}
