package com.zincoid.nullbot.core.model.data.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.model.data.po.InventoryPO;

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
