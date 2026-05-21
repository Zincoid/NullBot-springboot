package com.zincoid.nullbot.core.model.data.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.model.data.po.FilePO;

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
