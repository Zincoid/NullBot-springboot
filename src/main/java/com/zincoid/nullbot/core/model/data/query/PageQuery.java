package com.zincoid.nullbot.core.model.data.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public abstract class PageQuery {

    private Integer current = 1;
    private Integer size = 10;
    private String sortBy;
    private boolean isAsc = true;

    public <T> Page<T> toPage(OrderItem ...items) {
        Page<T> page = Page.of(current, size);
        if (StringUtils.hasText(sortBy))
            page.addOrder(isAsc ? OrderItem.asc(sortBy) : OrderItem.desc(sortBy));
        else if (items != null)
            page.addOrder(items);
        return page;
    }

    public <T> Page<T> toPage() {
        return toPage((OrderItem) null);
    }
}
