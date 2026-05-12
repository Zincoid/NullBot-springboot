package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.DriftBottlePO;

public interface DriftBottleService {

    boolean throwBottle(DriftBottlePO bottle);

    boolean throwBottle(Long userId, String userName, String content, Boolean isImage);

    DriftBottlePO pickUpRand();
}
