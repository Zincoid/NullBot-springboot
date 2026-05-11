package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.GroupPO;

import java.util.List;

@Data
@AllArgsConstructor
public class GroupPage {
    private List<GroupPO> groups;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
