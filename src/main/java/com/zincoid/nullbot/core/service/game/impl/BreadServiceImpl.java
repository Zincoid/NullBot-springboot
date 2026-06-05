package com.zincoid.nullbot.core.service.game.impl;

import com.zincoid.nullbot.core.service.basic.ItemService;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.enums.Category;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.service.game.BreadService;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.UserService;
import com.zincoid.nullbot.core.util.DrawUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class BreadServiceImpl implements BreadService {

    private final ItemService itemService;
    private final UserService userService;
    private final InventoryService inventoryService;

    // =================== 面包游戏相关 ===================

    @Override
    public List<InventoryVO> listVO(Long userId) {
        return inventoryService.listVO(userId).stream()
                .filter(vo -> vo.getCategory() == Category.BREAD)
                .toList();
    }

    @Override
    @Transactional
    public int buyBasic(Long userId, int cost) {  // 花费 cost 现金购买随机数量普通面包
        UserPO user = userService.getById(userId);
        if (user.getCash() >= cost) {
            ItemPO bread = getBasicBread();
            int i = ThreadLocalRandom.current().nextInt(10) + 1;
            if (inventoryService.add(userId, bread.getId(), i)) {
                user.setCash(user.getCash() - cost);
                userService.updateById(user);
                return i;
            } else
                return 0;
        } else
            return 0;
    }

    @Override
    @Transactional
    public int[] eatBasic(Long userId, int exp) {  // 吃随机 i 个普通面包并获得 i * exp 经验
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryService.lambdaQuery()
                .eq(InventoryPO::getOwnerId, userId)
                .eq(InventoryPO::getItemId, bread.getId())
                .one();
        if (userBread == null) return new int[]{0, 0};
        int i = Math.min(ThreadLocalRandom.current().nextInt(10) + 1, userBread.getAmount());
        if (inventoryService.remove(userId, bread.getId(), i)) {
            int j = userService.plusExperience(userId, i * exp);
            return new int[]{i, j};  // 返回: 实际吃掉个数 和 升级级数
        } else
            return new int[]{0, 0};
    }

    @Override
    @Transactional
    public boolean eatRotten(Long userId) {
        ItemPO bread = getBasicBread();
        UserPO user = userService.getById(userId);
        if (inventoryService.remove(userId, bread.getId(), 1)) {
            user.setExperience(0);
            userService.updateById(user);
            return true;
        } else
            return false;
    }

    @Override
    @Transactional
    public ItemPO buySpecial(Long userId, int cost) {  // 花费 cost 现金购买一个特殊面包
        UserPO user = userService.getById(userId);
        if (user.getCash() >= cost) {
            Rarity rarity = DrawUtil.drawRarityByProbability();
            List<ItemPO> itemList = itemService.lambdaQuery()
                    .eq(ItemPO::getCategory, Category.BREAD)
                    .eq(ItemPO::getAvailable, true)
                    .eq(ItemPO::getRarity, rarity)
                    .list();
            ItemPO item = DrawUtil.drawItemByLogPrice(itemList);
            if (inventoryService.add(userId, item.getId(), 1)) {
                user.setCash(user.getCash() - cost);
                userService.updateById(user);
                return item;
            } else
                return null;
        } else
            return null;
    }

    @Override
    @Transactional
    public int transferBasic(Long fromId, Long toId) {
        ItemPO bread = getBasicBread();
        InventoryPO userBread = inventoryService.lambdaQuery()
                .eq(InventoryPO::getOwnerId, fromId)
                .eq(InventoryPO::getItemId, bread.getId())
                .one();
        if (userBread == null) return 0;
        int i = Math.min(ThreadLocalRandom.current().nextInt(10) + 1, userBread.getAmount());
        if (inventoryService.remove(fromId, bread.getId(), i)) {
            inventoryService.add(toId, bread.getId(), i);
            return i;
        } else
            return 0;
    }

    private ItemPO getBasicBread() {
        return itemService.lambdaQuery()
                .eq(ItemPO::getCategory, Category.BREAD)
                .eq(ItemPO::getAvailable, false)
                .one();
    }
}
