package com.zincoid.nullbot.core.model.dto.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.model.po.ItemPO;

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
