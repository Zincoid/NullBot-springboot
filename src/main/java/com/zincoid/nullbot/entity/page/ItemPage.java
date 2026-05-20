package com.zincoid.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.entity.po.ItemPO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class ItemPage {
    private List<ItemPO> items;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
