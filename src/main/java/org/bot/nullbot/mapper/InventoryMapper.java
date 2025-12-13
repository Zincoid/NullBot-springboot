package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.entity.po.InventoryPO;


@Mapper
public interface InventoryMapper extends BaseMapper<InventoryPO>
{
}
