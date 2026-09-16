package com.zincoid.nullbot.core.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.model.data.query.SayingQuery;

public interface SayingService extends IService<SayingPO> {

    boolean add(Long userId, String userName, String text);

    void delete(Integer id);

    PageResult<SayingPO> page(SayingQuery query);

    SayingPO random();

    SayingPO random(Long userId);
}
