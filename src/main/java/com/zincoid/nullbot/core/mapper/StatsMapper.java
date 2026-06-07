package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.data.po.StatsPO;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper extends BaseMapper<StatsPO> {

    @Select("SELECT group_id, SUM(visits) as total_visits " +
            "FROM stats " +
            "GROUP BY group_id " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopGroups(@Param("limit") int limit);

    @Select("SELECT s.user_id, u.name AS user_name, SUM(s.visits) as total_visits " +
            "FROM stats s " +
            "JOIN user u ON s.user_id = u.id " +
            "GROUP BY s.user_id, u.name " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopUsers(@Param("limit") int limit);

    @Select("SELECT command, SUM(visits) as total_visits " +
            "FROM stats " +
            "GROUP BY command " +
            "ORDER BY total_visits DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectTopCommands(@Param("limit") int limit);

    @Select("SELECT SUM(visits) FROM stats WHERE user_id = #{userId}")
    Long selectUses(Long userId);
}
