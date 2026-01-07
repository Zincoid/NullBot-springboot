package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.ItemPO;

public interface ItemService
{
    ItemPO getItem(Integer itemId);

    ItemPO getRandomItem();

    ItemPO getRandomHighValueItem();

    ItemPO drawAndKeepRandomItem(Long userId);

    boolean exist(Integer itemId);

    boolean isUsable(Integer itemId);

    String getItemCommand(Integer itemId);
}
