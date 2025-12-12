package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.InventoryPO;
import org.bot.nullbot.dao.po.ItemPO;

import java.util.List;

public interface InventoryService
{
    List<InventoryPO> getInventories(Long userId);

    List<InventoryPO> getInventoriesPage(Long userId, int i);

    boolean increaseInventory(Long userId, ItemPO item);

    boolean decreaseInventory(Long userId, Integer itemId);
}
