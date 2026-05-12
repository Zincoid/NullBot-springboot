package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.vo.InventoryVO;

import java.util.List;

public interface BreadService {

    List<InventoryVO> getVOList(Long userId);

    int buyBasic(Long userId, int cost);

    int[] eatBasic(Long userId, int exp);

    boolean eatRotten(Long userId);

    ItemPO buySpecial(Long userId, int cost);

    int transferBasic(Long fromId, Long toId);
}
