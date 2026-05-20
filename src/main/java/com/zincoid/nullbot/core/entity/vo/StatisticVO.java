package com.zincoid.nullbot.core.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticVO {
    private Long totalVisits;

    private List<String> visitsXAxis;
    private List<Long> visitsData;

    private List<String> topGroupsAxis;     // 前10 group的X轴（group_id）
    private List<Long> topGroupsData;       // 前10 group的访问量

    private List<String> topUsersAxis;      // 前10 user的X轴（user_name + user_id）
    private List<Long> topUsersData;        // 前10 user的访问量

    private List<String> topCommandsAxis;   // 前10 command的X轴（command）
    private List<Long> topCommandsData;     // 前10 command的访问量
}
