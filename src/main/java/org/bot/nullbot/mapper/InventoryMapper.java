package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.bot.nullbot.entity.po.InventoryPO;

import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryPO>
{
    @Select("SELECT coalesce(sum(amount), 0) FROM inventory WHERE owner_id = #{userId}")
    int sumAmountByUserId(Long userId);

    @Select("SELECT * FROM inventory WHERE owner_id = #{userId} AND item_id = #{itemId} FOR UPDATE")
    List<InventoryPO> selectForUpdate(Long userId, Integer itemId);
}
