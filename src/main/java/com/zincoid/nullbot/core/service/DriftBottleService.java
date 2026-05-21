package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.po.DriftBottlePO;

public interface DriftBottleService {

    boolean throwBottle(DriftBottlePO bottle);

    boolean throwBottle(Long userId, String userName, String content, boolean isImage);

    DriftBottlePO pickUpRand();
}
