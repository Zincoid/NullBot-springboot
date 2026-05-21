package com.zincoid.nullbot.core.service;

import com.zincoid.nullbot.core.model.dto.page.DataPage;
import com.zincoid.nullbot.core.model.po.SayingPO;

import java.util.List;

public interface SayingService {

    int add(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    SayingPO getRand();

    SayingPO getRandByUserId(Long userId);

    List<SayingPO> getList();

    DataPage<SayingPO> getPage(Integer current, Integer size);

    void adds(List<SayingPO> sayings);
}
