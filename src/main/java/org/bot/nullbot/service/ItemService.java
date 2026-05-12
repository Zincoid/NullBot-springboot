package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.ItemPO;

import java.util.List;

public interface ItemService {

    ItemPO get(Integer id);

    ItemPO getRandom();

    ItemPO getRandomHighValue();

    ItemPO drawAndKeepRandom(Long userId);

    boolean exist(Integer id);

    boolean isUsable(Integer id);

    String getCommand(Integer id);

    List<ItemPO> getAll();

    DataPage<ItemPO> getPage(Integer current, Integer size);

    boolean add(ItemPO item);

    void adds(List<ItemPO> items);

    boolean update(ItemPO item);

    boolean deleteById(Integer id);
}
