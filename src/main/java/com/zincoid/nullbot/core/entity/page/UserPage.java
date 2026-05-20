package com.zincoid.nullbot.core.entity.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.entity.po.UserPO;

import java.util.List;

@Data
@AllArgsConstructor
@Deprecated
public class UserPage {
    private List<UserPO> users;
    private long currentPage;
    private long totalPage;
    private long total;
    private long pageSize;
}
