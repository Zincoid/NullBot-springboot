package org.bot.nullbot.service;

import org.bot.nullbot.dao.po.ItemPO;

public interface ItemService
{
    ItemPO getAndKeepRandomItem(Long userId);
}
