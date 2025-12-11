package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.ItemPO;

public interface ItemService
{
    ItemPO getAndKeepRandomItem(Long userId);

    boolean exist(Integer itemId);

    boolean isUsable(Integer itemId);

    String getCommandFromItemDesc(Integer itemId);
}
