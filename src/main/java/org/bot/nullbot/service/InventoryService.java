package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.DataPage;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.vo.InventoryVO;
import org.bot.nullbot.enums.Rarity;

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

    List<InventoryPO> getAll();

    void add(List<InventoryPO> inventories);

    boolean delete(Integer id);

    boolean update(InventoryPO inventory);
}
