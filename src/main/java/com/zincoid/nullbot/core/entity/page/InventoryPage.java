package com.zincoid.nullbot.core.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.entity.po.InventoryPO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class InventoryPage {
    private List<InventoryPO> inventories;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
