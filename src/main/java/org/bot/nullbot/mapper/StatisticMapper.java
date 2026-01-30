package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.bot.nullbot.entity.po.StatisticPO;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticMapper extends BaseMapper<StatisticPO>
{
    @Select("SELECT group_id, SUM(visits) as total_visits " +
            "FROM statistic " +
            "GROUP BY group_id " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopGroups(@Param("limit") int limit);

    @Select("SELECT user_id, SUM(visits) as total_visits " +
            "FROM statistic " +
            "GROUP BY user_id " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopUsers(@Param("limit") int limit);

    @Select("SELECT command, SUM(visits) as total_visits " +
            "FROM statistic " +
            "GROUP BY command " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopCommands(@Param("limit") int limit);
}
