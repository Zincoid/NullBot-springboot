package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.bot.nullbot.entity.po.DriftBottlePO;

@Mapper
public interface DriftBottleMapper extends BaseMapper<DriftBottlePO> {

    @Deprecated
    @Select("SELECT * FROM drift_bottle ORDER BY RAND() LIMIT 1")
    DriftBottlePO getRand();

    @Select("SELECT * FROM drift_bottle LIMIT 1 OFFSET #{offset}")
    DriftBottlePO getOneByOffset(long offset);
}
