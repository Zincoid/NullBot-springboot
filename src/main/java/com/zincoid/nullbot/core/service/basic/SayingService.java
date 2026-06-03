package com.zincoid.nullbot.core.service.basic;

import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.model.data.query.SayingQuery;

import java.util.List;

public interface SayingService {

    boolean add(Long userId, String userName, String text);

    boolean deleteById(Integer id);

    SayingPO getRand();

    SayingPO getRandByUserId(Long userId);

    List<SayingPO> getList();

    PageResult<SayingPO> getPage(SayingQuery query);

    void adds(List<SayingPO> sayings);
}
