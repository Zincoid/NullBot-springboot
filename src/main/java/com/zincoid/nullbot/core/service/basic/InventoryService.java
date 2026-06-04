package com.zincoid.nullbot.core.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.enums.Rarity;

import java.util.List;

public interface InventoryService extends IService<InventoryPO> {

    List<InventoryVO> getVOList(Long userId);

    PageResult<InventoryVO> getVOPage(Long userId, Integer current, Integer size);

    int getTotalAmount(Long userId);

    boolean increase(Long userId, Integer itemId, int i);

    boolean decrease(Long userId, Integer itemId, int i);

    boolean sell(Long userId, Integer itemId, int i);

    boolean buy(Long userId, Integer itemId, int i);

    boolean sellByRarity(Long userId, Rarity rarity);
}
