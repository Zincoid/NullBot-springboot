package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.page.DataPage;
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
public class BreadServiceImpl implements BreadService {

    private final UserMapper userMapper;
    private final InventoryMapper inventoryMapper;
    private final ItemMapper itemMapper;

    private final UserService userService;
    private final InventoryService inventoryService;

    private final Random random =  new Random();

    // =================== 面包游戏相关 ===================

    @Override
    public DataPage<InventoryPO> getBreadPage(Long userId, int p, int size) {
        Page<InventoryPO> page = new Page<>(p, size);
        Page<InventoryPO> inventoryPage = inventoryMapper
                .selectPage(page, new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, userId)
                        .eq(InventoryPO::getCategory, Category.BREAD)
                        .orderByDesc(InventoryPO::getRarity)
                        .orderByDesc(InventoryPO::getPrice)
                        .orderByAsc(InventoryPO::getId));
        return new DataPage<>(inventoryPage.getRecords(), inventoryPage.getCurrent(), inventoryPage.getPages(), inventoryPage.getTotal(), inventoryPage.getSize());
    }

    @Override
    @Transactional
    public int buyBasicBread(Long userId, int cost) {  // 花费 cost 现金购买随机数量普通面包
        UserPO user = userMapper.selectById(userId);
        if(user.getCash() >= cost){
            ItemPO bread = getBasicBread();
            int i = random.nextInt(10) + 1;
            if(inventoryService.increaseInventory(userId, bread.getId(), i)){
                user.setCash(user.getCash() - cost);
                userMapper.updateById(user);
                return i;
            }else
                return 0;
        }else
            return 0;
    }

    @Override
    @Transactional
    public int[] eatBasicBread(Long userId, int exp) {  // 吃随机 i 个普通面包并获得 i * exp 经验
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryMapper
                .selectOne(new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, userId)
                        .eq(InventoryPO::getItemId, bread.getId())
                );
        if(userBread == null) return new int[]{0, 0};
        int i = Math.min(random.nextInt(10) + 1, userBread.getAmount());
        if(inventoryService.decreaseInventory(userId, bread.getId(), i)){
            int j = userService.plusExperience(userId, i * exp);
            return new int[]{i, j};  // 返回: 实际吃掉个数 和 升级级数
        } else
            return new int[]{0, 0};
    }

    @Override
    @Transactional
    public boolean eatRottenBread(Long userId) {
        ItemPO bread = getBasicBread();
        UserPO user = userMapper.selectById(userId);
        if(inventoryService.decreaseInventory(userId, bread.getId(), 1)){
            user.setExperience(0);
            userMapper.updateById(user);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public ItemPO buySpecialBread(Long userId, int cost) {  // 花费 cost 现金购买一个特殊面包
        UserPO user = userMapper.selectById(userId);
        if(user.getCash() >= cost){

            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemMapper
                    .selectList(new LambdaQueryWrapper<ItemPO>()
                            .eq(ItemPO::getCategory, Category.BREAD)
                            .eq(ItemPO::getAvailable, true)
                            .eq(ItemPO::getRarity, rarity)
                    );
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if(inventoryService.increaseInventory(userId, item.getId(), 1)){
                user.setCash(user.getCash() - cost);
                userMapper.updateById(user);
                return item;
            } else
                return null;
        }else
            return null;
    }

    @Override
    @Transactional
    public int transferBasicBread(Long fromId, Long toId) {
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryMapper
                .selectOne(new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, fromId)
                        .eq(InventoryPO::getItemId, bread.getId())
                );
        if(userBread == null) return 0;
        int i = Math.min(random.nextInt(10) + 1, userBread.getAmount());
        if(inventoryService.decreaseInventory(fromId, bread.getId(), i)){
            inventoryService.increaseInventory(toId, bread.getId(), i);
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
