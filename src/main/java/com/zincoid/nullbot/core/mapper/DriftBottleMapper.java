package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.data.po.DriftBottlePO;

@Mapper
public interface DriftBottleMapper extends BaseMapper<DriftBottlePO> {

    @Select("SELECT * FROM drift_bottle LIMIT 1 OFFSET #{offset}")
    DriftBottlePO getOneByOffset(long offset);
}
