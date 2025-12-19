package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.InventoryPO;

import java.util.List;

@Data
@AllArgsConstructor
public class InventoryPage
{
    private List<InventoryPO> inventories;
    private long currentPage;
    private long totalPage;
    private long pageSize;
}
