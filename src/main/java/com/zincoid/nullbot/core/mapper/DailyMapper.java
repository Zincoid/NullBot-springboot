package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.data.po.DailyPO;

@Mapper
public interface DailyMapper extends BaseMapper<DailyPO> {

    @Select("SELECT COALESCE(SUM(visits), 0) FROM daily")
    Long selectTotalVisits();
}
