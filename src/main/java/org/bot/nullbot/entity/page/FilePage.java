package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.FilePO;

import java.util.List;

@Data
@AllArgsConstructor
public class FilePage
{
    private List<FilePO> files;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
