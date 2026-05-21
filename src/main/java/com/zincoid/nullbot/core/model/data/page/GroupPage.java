package com.zincoid.nullbot.core.model.data.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.model.data.po.GroupPO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class GroupPage {
    private List<GroupPO> groups;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
