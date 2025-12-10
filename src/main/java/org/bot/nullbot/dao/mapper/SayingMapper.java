package org.bot.nullbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.bot.nullbot.dao.po.SayingPO;

import java.util.List;


@Mapper
public interface SayingMapper extends BaseMapper<SayingPO>
{
    @Select("SELECT * FROM saying")
    List<SayingPO> getList();

    @Delete("DELETE FROM saying WHERE id = #{id}")
    boolean deleteById(Integer id);

    @Insert("INSERT INTO saying(user_id, user_name, text) VALUES(#{userId}, #{userName}, #{text})")
    int insert(Long userId, String userName, String text);

    @Select("SELECT * FROM saying ORDER BY RAND() LIMIT 1")
    SayingPO getRand();
}
