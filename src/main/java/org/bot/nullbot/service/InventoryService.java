package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.game.basic.InventoryPage;

import java.util.List;

public interface InventoryService
{
    List<InventoryPO> getInventories(Long userId);

    InventoryPage getInventoriesPage(Long userId, long p, long size);

    boolean increaseInventory(Long userId, ItemPO item);

    boolean decreaseInventory(Long userId, Integer itemId);
}
