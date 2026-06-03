package com.zincoid.nullbot.core.model.data.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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

    public static StatisticVO of(
            Long totalVisits, List<String> visitsXAxis, List<Long> visitsData,
            List<String> topGroupsAxis, List<Long> topGroupsData,
            List<String> topUsersAxis, List<Long> topUsersData,
            List<String> topCommandsAxis, List<Long> topCommandsData
    ) {
        StatisticVO vo = new StatisticVO();
        vo.setTotalVisits(totalVisits);
        vo.setVisitsXAxis(visitsXAxis);
        vo.setVisitsData(visitsData);
        vo.setTopGroupsAxis(topGroupsAxis);
        vo.setTopGroupsData(topGroupsData);
        vo.setTopUsersAxis(topUsersAxis);
        vo.setTopUsersData(topUsersData);
        vo.setTopCommandsAxis(topCommandsAxis);
        vo.setTopCommandsData(topCommandsData);
        return vo;
    }
}
