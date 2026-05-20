package com.zincoid.nullbot.service;

import com.zincoid.nullbot.entity.po.DriftBottlePO;

public interface DriftBottleService {

    boolean throwBottle(DriftBottlePO bottle);

    boolean throwBottle(Long userId, String userName, String content, boolean isImage);

    DriftBottlePO pickUpRand();
}
