package com.zincoid.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.entity.po.InventoryPO;
import com.zincoid.nullbot.entity.po.ItemPO;
import com.zincoid.nullbot.entity.po.UserPO;
import com.zincoid.nullbot.entity.vo.InventoryVO;
import com.zincoid.nullbot.enums.Category;
import com.zincoid.nullbot.enums.Rarity;
import com.zincoid.nullbot.mapper.InventoryMapper;
import com.zincoid.nullbot.mapper.ItemMapper;
import com.zincoid.nullbot.mapper.UserMapper;
import com.zincoid.nullbot.service.BreadService;
import com.zincoid.nullbot.service.InventoryService;
import com.zincoid.nullbot.service.UserService;
import com.zincoid.nullbot.util.DrawUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
                .filter(inventoryVO -> inventoryVO.getCategory() == Category.BREAD)
                .sorted(Comparator
                        .comparing(InventoryVO::getRarity, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getPrice, Comparator.reverseOrder())
                        .thenComparing(InventoryVO::getId)
                )
                .toList();
    }

    @Override
    @Transactional
    public int buyBasic(Long userId, int cost) {  // 花费 cost 现金购买随机数量普通面包
        UserPO user = userMapper.selectById(userId);
        if(user.getCash() >= cost){
            ItemPO bread = getBasicBread();
            int i = random.nextInt(10) + 1;
            if(inventoryService.increase(userId, bread.getId(), i)){
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
    public int[] eatBasic(Long userId, int exp) {  // 吃随机 i 个普通面包并获得 i * exp 经验
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryMapper
                .selectOne(new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, userId)
                        .eq(InventoryPO::getItemId, bread.getId())
                );
        if(userBread == null) return new int[]{0, 0};
        int i = Math.min(random.nextInt(10) + 1, userBread.getAmount());
        if(inventoryService.decrease(userId, bread.getId(), i)){
            int j = userService.plusExperience(userId, i * exp);
            return new int[]{i, j};  // 返回: 实际吃掉个数 和 升级级数
        } else
            return new int[]{0, 0};
    }

    @Override
    @Transactional
    public boolean eatRotten(Long userId) {
        ItemPO bread = getBasicBread();
        UserPO user = userMapper.selectById(userId);
        if(inventoryService.decrease(userId, bread.getId(), 1)){
            user.setExperience(0);
            userMapper.updateById(user);
            return true;
        }else
            return false;
    }

    @Override
    @Transactional
    public ItemPO buySpecial(Long userId, int cost) {  // 花费 cost 现金购买一个特殊面包
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
            if(inventoryService.increase(userId, item.getId(), 1)){
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
    public int transferBasic(Long fromId, Long toId) {
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryMapper
                .selectOne(new LambdaQueryWrapper<InventoryPO>()
                        .eq(InventoryPO::getOwnerId, fromId)
                        .eq(InventoryPO::getItemId, bread.getId())
                );
        if(userBread == null) return 0;
        int i = Math.min(random.nextInt(10) + 1, userBread.getAmount());
        if(inventoryService.decrease(fromId, bread.getId(), i)){
            inventoryService.increase(toId, bread.getId(), i);
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
