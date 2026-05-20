package com.zincoid.nullbot.core.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.entity.po.SayingPO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class SayingPage {
    private List<SayingPO> sayings;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
