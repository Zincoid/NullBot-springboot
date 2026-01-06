package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.enums.Category;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.mapper.InventoryMapper;
import org.bot.nullbot.mapper.ItemMapper;
import org.bot.nullbot.mapper.UserMapper;
import org.bot.nullbot.service.BreadService;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.UserService;
import org.bot.nullbot.util.DrawUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class BreadServiceImpl implements BreadService
{
    private final UserMapper userMapper;
    private final InventoryMapper inventoryMapper;
    private final ItemMapper itemMapper;

    private final UserService userService;
    private final InventoryService inventoryService;

    private final Random random =  new Random();

    // =================== 面包游戏相关 ===================

    @Override
    @Transactional
    public int buyBasicBread(Long userId) {  // 花费 100 现金购买随机数量普通面包
        UserPO user = userMapper.selectById(userId);
        if(user.getCash() >= 100){
            user.setCash(user.getCash() - 100);
            userMapper.updateById(user);
            ItemPO bread = getBasicBread();
            int i = random.nextInt(10);
            if(inventoryService.increaseInventory(userId, bread.getId(), i)){
                return i;
            }else
                return 0;
        }else
            return 0;
    }

    @Override
    @Transactional
    public ItemPO buySpecialBread(Long userId) {  // 花费 100 现金购买一个特殊面包
        UserPO user = userMapper.selectById(userId);
        if(user.getCash() >= 100){
            user.setCash(user.getCash() - 100);
            userMapper.updateById(user);
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper
                    .selectList(new LambdaQueryWrapper<ItemPO>()
                            .eq(ItemPO::getCategory, Category.BREAD)
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
    @Transactional
    public int eatBasicBread(Long userId) {  // 吃随机数量普通面包并获得经验
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryMapper
                .selectOne(new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, userId)
                        .eq(InventoryPO::getItemId, bread.getId())
                );
        if(userBread == null) return 0;
        int i = Math.min(random.nextInt(10), userBread.getAmount());
        if(inventoryService.decreaseInventory(userId, bread.getId(), i)){
            userService.plusExperience(userId, i);
            return i;
        } else
            return 0;
    }

    private ItemPO getBasicBread() {
        return itemMapper
                .selectOne(new LambdaQueryWrapper<ItemPO>()
                        .eq(ItemPO::getCategory, Category.BREAD)
                        .eq(ItemPO::getAvailable, false)
                );
    }
}
