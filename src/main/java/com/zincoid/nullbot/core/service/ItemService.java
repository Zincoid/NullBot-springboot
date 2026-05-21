package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.dto.page.DataPage;
import com.zincoid.nullbot.core.model.po.ItemPO;

import java.util.List;

public interface ItemService {

    ItemPO get(Integer id);

    ItemPO getRandom();

    ItemPO getRandomHighValue();

    ItemPO drawAndKeepRandom(Long userId);

    boolean exist(Integer id);

    boolean isUsable(Integer id);

    String getCommand(Integer id);

    List<ItemPO> getList();

    DataPage<ItemPO> getPage(Integer current, Integer size);

    boolean add(ItemPO item);

    void adds(List<ItemPO> items);

    boolean update(ItemPO item);

    boolean deleteById(Integer id);
}
