package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.ItemPO;

public interface BreadService
{
    int buyBasicBread(Long userId);

    ItemPO buySpecialBread(Long userId);

    int eatBasicBread(Long userId);
}
