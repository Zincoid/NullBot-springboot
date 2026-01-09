package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.ItemPage;
import org.bot.nullbot.entity.po.ItemPO;

import java.util.List;

public interface ItemService
{
    ItemPO getItem(Integer itemId);

    ItemPO getRandomItem();

    ItemPO getRandomHighValueItem();

    ItemPO drawAndKeepRandomItem(Long userId);

    boolean exist(Integer itemId);

    boolean isUsable(Integer itemId);

    String getItemCommand(Integer itemId);

    List<ItemPO> getItemList();

    ItemPage getItemByPage(Integer currentPage, Integer pageSize);

    boolean addItem(ItemPO item);

    void addItems(List<ItemPO> items);

    boolean updateItem(ItemPO item);

    boolean deleteById(Integer id);
}
