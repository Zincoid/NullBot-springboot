package com.zincoid.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.zincoid.nullbot.entity.po.ItemPO;

@Mapper
public interface ItemMapper extends BaseMapper<ItemPO> {
}
