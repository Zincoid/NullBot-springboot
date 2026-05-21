package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.po.ItemPO;
import com.zincoid.nullbot.core.model.vo.InventoryVO;

import java.util.List;

public interface BreadService {

    List<InventoryVO> getVOList(Long userId);

    int buyBasic(Long userId, int cost);

    int[] eatBasic(Long userId, int exp);

    boolean eatRotten(Long userId);

    ItemPO buySpecial(Long userId, int cost);

    int transferBasic(Long fromId, Long toId);
}
