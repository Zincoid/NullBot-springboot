package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import com.zincoid.nullbot.core.entity.po.SayingPO;

@Mapper
public interface SayingMapper extends BaseMapper<SayingPO> {

    @Deprecated
    @Select("SELECT * FROM saying ORDER BY RAND() LIMIT 1")
    SayingPO getRand();

    @Select("SELECT * FROM saying LIMIT 1 OFFSET #{offset}")
    SayingPO getOneByOffset(long offset);

    @Select("SELECT * FROM saying WHERE user_id = #{userId} ORDER BY RAND() LIMIT 1")
    SayingPO getRandById(Long userId);
}
