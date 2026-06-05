package com.zincoid.nullbot.core.service.basic;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;

public interface ItemService extends IService<ItemPO> {

    PageResult<ItemPO> page(ItemQuery query);

    boolean exist(Integer id);

    boolean isUsable(Integer id);

    String getCommand(Integer id);

    ItemPO getRandom();

    ItemPO getRandomHighValue();
}
