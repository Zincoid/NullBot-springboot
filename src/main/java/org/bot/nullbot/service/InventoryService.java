package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.page.InventoryPage;
import org.bot.nullbot.enums.Rarity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InventoryService
{
    void updateAllInventories();

    List<InventoryPO> getInventories(Long userId);

    InventoryPage getInventoriesPage(Long userId, int p, int size);

    int getTotalAmountByUserId(Long userId);

    boolean increaseInventory(Long userId, Integer itemId,  int i);

    boolean decreaseInventory(Long userId, Integer itemId,  int i);

    boolean sellInventory(Long userId, Integer itemId, int i);

    boolean buyInventory(Long userId, Integer itemId, int i);

    boolean sellInventoryByRarity(Long userId, Rarity rarity);
}
