package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.ItemPO;

public interface InventoryService
{
    boolean increaseInventory(Long userId, ItemPO item);
}
