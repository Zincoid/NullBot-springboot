package com.zincoid.nullbot.core.model.data.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SayingQuery extends PageQuery {

    @Override
    public <T> Page<T> toPage() {
        return super.toPage(
                OrderItem.desc("time")
        );
    }
}
