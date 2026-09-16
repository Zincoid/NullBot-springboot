package com.zincoid.nullbot.core.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.data.query.ItemQuery;
import com.zincoid.nullbot.core.model.data.dto.ItemDTO;

public interface ItemService extends IService<ItemPO> {

    void add(ItemDTO item);

    void update(ItemDTO item);

    void delete(Integer id);

    PageResult<ItemPO> page(ItemQuery query);

    ItemPO getRandom();

    ItemPO getRandomHighValue();
}
