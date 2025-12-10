package org.bot.nullbot.service;

public interface InventoryService
{
    boolean increaseInventory(Long userId, Integer itemId);
}
