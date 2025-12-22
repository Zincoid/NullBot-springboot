package org.bot.nullbot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.entity.po.StatisticDatePO;
import org.bot.nullbot.entity.po.StatisticPO;
import org.bot.nullbot.mapper.StatisticDateMapper;
import org.bot.nullbot.mapper.StatisticMapper;
import org.bot.nullbot.service.StatisticService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

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
}
