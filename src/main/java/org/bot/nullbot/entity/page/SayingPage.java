package org.bot.nullbot.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.SayingPO;

import java.util.List;

@Data
@AllArgsConstructor
public class SayingPage {
    private List<SayingPO> sayings;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
