package com.zincoid.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.entity.po.GroupPO;

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
