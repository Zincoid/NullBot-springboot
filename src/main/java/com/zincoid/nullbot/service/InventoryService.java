package com.zincoid.nullbot.service;

import com.zincoid.nullbot.entity.page.DataPage;
import com.zincoid.nullbot.entity.po.InventoryPO;
import com.zincoid.nullbot.entity.vo.InventoryVO;
import com.zincoid.nullbot.enums.Rarity;

import java.util.List;

public interface InventoryService {

    DataPage<InventoryVO> getVOPage(Long userId, Integer current, Integer size);

    int getTotalAmount(Long userId);

    boolean increase(Long userId, Integer itemId, int i);

    boolean decrease(Long userId, Integer itemId, int i);

    boolean sell(Long userId, Integer itemId, int i);

    boolean buy(Long userId, Integer itemId, int i);

    boolean sellByRarity(Long userId, Rarity rarity);

    List<InventoryVO> getVOList(Long userId);

    List<InventoryPO> getList();

    void adds(List<InventoryPO> inventories);

    boolean deleteById(Integer id);

    boolean update(InventoryPO inventory);
}
