package com.zincoid.nullbot.core.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.model.data.query.SayingQuery;

public interface SayingService extends IService<SayingPO> {

    PageResult<SayingPO> getPage(SayingQuery query);

    boolean addSaying(Long userId, String userName, String text);

    SayingPO getRand();

    SayingPO getRandByUserId(Long userId);
}
