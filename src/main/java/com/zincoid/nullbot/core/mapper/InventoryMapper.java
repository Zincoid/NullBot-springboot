package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.entity.po.InventoryPO;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryPO> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM inventory WHERE owner_id = #{userId}")
    Integer sumAmountByUserId(Long userId);
}
