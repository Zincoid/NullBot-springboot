package com.zincoid.nullbot.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.model.po.StatisticDatePO;
import com.zincoid.nullbot.core.model.po.StatisticPO;
import com.zincoid.nullbot.core.model.po.UserPO;
import com.zincoid.nullbot.core.model.vo.StatisticVO;
import com.zincoid.nullbot.core.mapper.StatisticDateMapper;
import com.zincoid.nullbot.core.mapper.StatisticMapper;
import com.zincoid.nullbot.core.mapper.UserMapper;
import com.zincoid.nullbot.core.service.StatisticService;
import org.springframework.beans.factory.annotation.Value;
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
public class StatisticServiceImpl implements StatisticService {

    @Value("${nullbot.bot-id}")
    private Long botId;
    private final BotContainer botContainer;

    private final StatisticMapper statisticMapper;
    private final StatisticDateMapper statisticDateMapper;
    private final UserMapper userMapper;

    // =================== WEB功能相关 ===================

    @Override
    @Transactional
    public void increaseOnDate() {
        List<StatisticDatePO> statisticDates = statisticDateMapper.selectList(new LambdaQueryWrapper<StatisticDatePO>().eq(StatisticDatePO::getDate, LocalDate.now()));
        if (statisticDates == null || statisticDates.isEmpty())
            statisticDateMapper.insert(new StatisticDatePO(null, LocalDate.now(), 1L));
        else{
            StatisticDatePO statisticDate = statisticDates.getFirst();
            statisticDate.setVisits(statisticDate.getVisits() + 1);
            statisticDateMapper.updateById(statisticDate);
        }
    }

    @Override
    @Transactional
    public void increase(Long groupId, Long userId, String userName, String command) {
        List<StatisticPO> statistics = statisticMapper.selectList(new LambdaQueryWrapper<StatisticPO>().eq(StatisticPO::getGroupId, groupId).eq(StatisticPO::getUserId, userId).eq(StatisticPO::getCommand, command));
        if (statistics == null || statistics.isEmpty())
            statisticMapper.insert(new StatisticPO(null, groupId, userId, userName, command, 1L));
        else{
            StatisticPO statistic = statistics.getFirst();
            statistic.setUserName(userName);
            statistic.setVisits(statistic.getVisits() + 1);
            statisticMapper.updateById(statistic);
        }
    }

    @Override
    @Transactional
    public StatisticVO getStatistic() {
        Long totalVisit = statisticDateMapper.selectTotalVisits();

        // 获取当前日期和10天前的日期
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29); // 包括今天，共30天

        // 查询最近10天的数据
        List<StatisticDatePO> recentData = statisticDateMapper.selectList(
                new LambdaQueryWrapper<StatisticDatePO>()
                        .ge(StatisticDatePO::getDate, startDate)
                        .le(StatisticDatePO::getDate, endDate)
                        .orderByAsc(StatisticDatePO::getDate)
        );

        // 将查询结果转换为 Map，方便查找
        Map<LocalDate, Long> dataMap = recentData.stream()
                .collect(Collectors.toMap(
                        StatisticDatePO::getDate,
                        StatisticDatePO::getVisits
                ));

        // 生成完整的10天日期列表
        List<String> xAxis = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 添加到 X 轴
            xAxis.add(currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));

            // 获取访问量，如果没有数据则为 0
            Long visits = dataMap.getOrDefault(currentDate, 0L);
            data.add(visits);

            currentDate = currentDate.plusDays(1);
        }

        // 查询访问次数最多的group
        List<Map<String, Object>> topGroups = statisticMapper.selectTopGroups(20);
        List<String> groupAxis = new ArrayList<>();
        List<Long> groupData = new ArrayList<>();
        for (Map<String, Object> map : topGroups) {
            groupAxis.add(String.valueOf(map.get("group_id")));
            groupData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 查询访问次数最多的user
        Bot bot = botContainer.robots.get(botId);
        List<Map<String, Object>> topUsers = statisticMapper.selectTopUsers(20);
        List<String> userAxis = new ArrayList<>();
        List<Long> userData = new ArrayList<>();
        for (Map<String, Object> map : topUsers) {
            long userId = Long.parseLong(map.get("user_id").toString());
            String userName;
            UserPO user = userMapper.selectById(userId);
            if (user != null) {
                userName = user.getName();
            } else {
                userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            }
            userAxis.add(userName + "\n(" + userId + ")");
            userData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 查询访问次数最多的command
        List<Map<String, Object>> topCommands = statisticMapper.selectTopCommands(20);
        List<String> commandAxis = new ArrayList<>();
        List<Long> commandData = new ArrayList<>();
        for (Map<String, Object> map : topCommands) {
            commandAxis.add(String.valueOf(map.get("command")));
            commandData.add(Long.valueOf(map.get("total_visits").toString()));
        }

        // 封装为 VO
        StatisticVO vo = new StatisticVO();
        vo.setTotalVisits(totalVisit);
        vo.setVisitsXAxis(xAxis);
        vo.setVisitsData(data);
        vo.setTopGroupsAxis(groupAxis);
        vo.setTopGroupsData(groupData);
        vo.setTopUsersAxis(userAxis);
        vo.setTopUsersData(userData);
        vo.setTopCommandsAxis(commandAxis);
        vo.setTopCommandsData(commandData);

        return vo;
    }
}
