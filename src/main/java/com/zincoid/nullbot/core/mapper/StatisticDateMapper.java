package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.po.StatisticDatePO;

@Mapper
public interface StatisticDateMapper extends BaseMapper<StatisticDatePO> {

    @Select("SELECT COALESCE(SUM(visits), 0) FROM statistic_date")
    Long selectTotalVisits();
}
