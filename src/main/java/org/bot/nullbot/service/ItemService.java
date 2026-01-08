package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.ItemPage;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.enums.Category;

public interface ItemService
{
    ItemPO getItem(Integer itemId);

    ItemPO getRandomItem();

    ItemPO getRandomHighValueItem();

    ItemPO drawAndKeepRandomItem(Long userId);

    boolean exist(Integer itemId);

    boolean isUsable(Integer itemId);

    String getItemCommand(Integer itemId);

    ItemPage getItemByPage(Integer currentPage, Integer pageSize, Category category);

    boolean updateItem(ItemPO item);
}
