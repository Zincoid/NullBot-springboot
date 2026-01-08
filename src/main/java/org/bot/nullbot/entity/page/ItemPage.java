package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.ItemPO;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemPage
{
    private List<ItemPO> items;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
