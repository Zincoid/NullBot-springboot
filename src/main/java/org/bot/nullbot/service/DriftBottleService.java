package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.DriftBottlePO;

public interface DriftBottleService
{
    int throwBottle(Long userId, String userName, String text);

    DriftBottlePO pickUpRand();
}
