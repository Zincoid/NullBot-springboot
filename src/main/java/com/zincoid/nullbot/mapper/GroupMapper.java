package com.zincoid.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.zincoid.nullbot.entity.po.GroupPO;

@Mapper
public interface GroupMapper extends BaseMapper<GroupPO> {
}
