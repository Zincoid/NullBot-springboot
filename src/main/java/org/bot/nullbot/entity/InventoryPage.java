package org.bot.nullbot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.dao.po.InventoryPO;

import java.util.List;

@Data
@AllArgsConstructor
public class InventoryPage
{
    List<InventoryPO> inventories;
    long currentPage;
    long totalPage;
    long pageSize;
}
