package com.zincoid.nullbot.core.service.game;

import com.zincoid.nullbot.core.model.data.po.DriftBottlePO;

public interface DriftBottleService {

    boolean add(DriftBottlePO bottle);

    boolean add(Long userId, String userName, String content, boolean isImage);

    DriftBottlePO pick();
}
