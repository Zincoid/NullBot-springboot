package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.page.InventoryPage;
import org.bot.nullbot.enums.Rarity;

import java.util.List;

public interface InventoryService
{
    List<InventoryPO> getInventories(Long userId);

    InventoryPage getInventoriesPage(Long userId, long p, long size);

    boolean increaseInventory(Long userId, Integer itemId,  int i);

    boolean decreaseInventory(Long userId, Integer itemId,  int i);

    boolean sellInventory(Long userId, int itemId, int i);

    boolean sellInventoryByRarity(Long userId, Rarity rarity);

    int getTotalAmountByUserId(Long userId);
}
