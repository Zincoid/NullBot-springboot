package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.data.po.BottlePO;

@Mapper
public interface BottleMapper extends BaseMapper<BottlePO> {

    @Select("SELECT * FROM bottle LIMIT 1 OFFSET #{offset}")
    BottlePO getOneByOffset(long offset);
}
