package com.zincoid.nullbot.core.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.data.po.DailyPO;
import com.zincoid.nullbot.core.model.data.po.StatsPO;
import com.zincoid.nullbot.core.model.data.vo.StatsVO;
import com.zincoid.nullbot.core.mapper.DailyMapper;
import com.zincoid.nullbot.core.mapper.StatsMapper;
import com.zincoid.nullbot.core.service.system.StatsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;
    private final DailyMapper dailyMapper;

    @Override
    @Transactional
    public void increaseDaily() {
        DailyPO dailyPO = dailyMapper.selectOne(new LambdaQueryWrapper<DailyPO>().eq(DailyPO::getDate, LocalDate.now()));
        if (dailyPO == null)
            dailyMapper.insert(new DailyPO(null, LocalDate.now(), 1L));
        else {
            dailyPO.setVisits(dailyPO.getVisits() + 1);
            dailyMapper.updateById(dailyPO);
        }
    }

    @Override
    @Transactional
    public void increase(Long groupId, Long userId, String command) {
        StatsPO statsPO = statsMapper.selectOne(new LambdaQueryWrapper<StatsPO>()
                .eq(StatsPO::getGroupId, groupId)
                .eq(StatsPO::getUserId, userId)
                .eq(StatsPO::getCommand, command));
        if (statsPO == null)
            statsMapper.insert(new StatsPO(null, groupId, userId, command, 1L));
        else {
            statsPO.setVisits(statsPO.getVisits() + 1);
            statsMapper.updateById(statsPO);
        }
    }

    @Override
    @Transactional
    public StatsVO getStatsVO() {
        Long totalVisit = dailyMapper.selectTotalVisits();

        // 获取当前日期和10天前的日期
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);  // 包括今天共30天

        // 查询最近10天的数据
        List<DailyPO> recentData = dailyMapper.selectList(
                new LambdaQueryWrapper<DailyPO>()
                        .ge(DailyPO::getDate, startDate)
                        .le(DailyPO::getDate, endDate)
                        .orderByAsc(DailyPO::getDate)
        );

        // 将查询结果转换为 Map，方便查找
        Map<LocalDate, Long> dataMap = recentData.stream()
                .collect(Collectors.toMap(
                        DailyPO::getDate,
                        DailyPO::getVisits
                ));

        // 生成完整的10天日期列表
        List<String> xAxis = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 添加到 X 轴
            xAxis.add(currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
            // 获取访问量 如果没有数据则为 0
            Long visits = dataMap.getOrDefault(currentDate, 0L);
            data.add(visits);
            currentDate = currentDate.plusDays(1);
        }

        // 查询访问次数最多的group
        List<Map<String, Object>> topGroups = statsMapper.selectTopGroups(20);
        List<String> groupAxis = new ArrayList<>();
        List<Long> groupData = new ArrayList<>();
        for (Map<String, Object> map : topGroups) {
            groupAxis.add(String.valueOf(map.get("group_id")));
            groupData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 查询访问次数最多的user
        List<Map<String, Object>> topUsers = statsMapper.selectTopUsers(20);
        List<String> userAxis = new ArrayList<>();
        List<Long> userData = new ArrayList<>();
        for (Map<String, Object> map : topUsers) {
            long userId = Long.parseLong(map.get("user_id").toString());
            String userName = map.get("user_name").toString();
            userAxis.add(userName + "\n(" + userId + ")");
            userData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 查询访问次数最多的command
        List<Map<String, Object>> topCommands = statsMapper.selectTopCommands(20);
        List<String> commandAxis = new ArrayList<>();
        List<Long> commandData = new ArrayList<>();
        for (Map<String, Object> map : topCommands) {
            commandAxis.add(String.valueOf(map.get("command")));
            commandData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 封装为 VO
        return StatsVO.of(totalVisit, xAxis, data,
                groupAxis, groupData, userAxis, userData, commandAxis, commandData);
    }

    @Override
    public Long getUsage(Long userId) {
        return statsMapper.selectUses(userId);
    }
}
