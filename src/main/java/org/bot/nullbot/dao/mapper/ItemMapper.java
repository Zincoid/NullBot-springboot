package org.bot.nullbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.dao.po.ItemPO;


@Mapper
public interface ItemMapper extends BaseMapper<ItemPO>
{
}
