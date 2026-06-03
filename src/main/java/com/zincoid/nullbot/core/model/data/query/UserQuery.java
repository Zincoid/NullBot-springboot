package com.zincoid.nullbot.core.model.data.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class UserQuery extends PageQuery {

    @Override
    public <T> Page<T> toPage() {
        return super.toPage(
                OrderItem.asc("id")
        );
    }
}
