package com.zincoid.nullbot.core.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.entity.po.FilePO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class FilePage {
    private List<FilePO> files;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
