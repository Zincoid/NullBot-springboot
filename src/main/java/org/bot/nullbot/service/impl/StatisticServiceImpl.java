package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.StatisticDatePO;
import org.bot.nullbot.entity.po.StatisticPO;
import org.bot.nullbot.entity.vo.StatisticVO;
import org.bot.nullbot.mapper.StatisticDateMapper;
import org.bot.nullbot.mapper.StatisticMapper;
import org.bot.nullbot.service.StatisticService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService
{
    private final StatisticMapper statisticMapper;
    private final StatisticDateMapper statisticDateMapper;

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
        // 获取当前日期和10天前的日期
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(9); // 包括今天，共10天

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

        // 封装为 VO
        StatisticVO vo = new StatisticVO();
        vo.setVisitsXAxis(xAxis);
        vo.setVisitsData(data);

        return vo;
    }
}
