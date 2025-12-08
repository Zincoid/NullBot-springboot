package org.bot.nullbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.bot.nullbot.dao.po.SayingPO;

import java.util.List;


@Mapper
public interface SayingMapper extends BaseMapper<SayingPO>
{
    @Select("select * from saying")
    List<SayingPO> getList();

    @Delete("delete from saying where id = #{id}")
    boolean deleteById(Integer id);

    @Insert("insert into saying(user_id, user_name, text) values(#{userId}, #{userName}, #{text})")
    int insert(Long userId, String userName, String text);

    @Select("select * from saying order by rand() limit 1")
    SayingPO getRand();
}
