package com.zincoid.nullbot.core.service.game;

import com.zincoid.nullbot.core.model.data.po.BottlePO;

public interface BottleService {

    boolean add(BottlePO bottle);

    boolean add(Long userId, String userName, String content, boolean isImage);

    BottlePO pick();
}
