package com.zincoid.nullbot.core.service.basic;

import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;

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

    PageResult<ItemPO> getPage(ItemQuery query);

    boolean add(ItemPO item);

    void adds(List<ItemPO> items);

    boolean update(ItemPO item);

    boolean deleteById(Integer id);
}
