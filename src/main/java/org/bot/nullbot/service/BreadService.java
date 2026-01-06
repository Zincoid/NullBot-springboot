package org.bot.nullbot.service;

import org.bot.nullbot.entity.page.InventoryPage;
import org.bot.nullbot.entity.po.ItemPO;

public interface BreadService
{
    InventoryPage getBreadPage(Long userId, int p, int size);

    int buyBasicBread(Long userId);

    int eatBasicBread(Long userId);

    ItemPO buySpecialBread(Long userId);
}
