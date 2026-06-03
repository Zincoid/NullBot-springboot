package com.zincoid.nullbot.core.model.data.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.OrderItem;

public class GroupQuery extends PageQuery {

    @Override
    public <T> Page<T> toPage() {
        return super.toPage(
                OrderItem.asc("id")
        );
    }
}
