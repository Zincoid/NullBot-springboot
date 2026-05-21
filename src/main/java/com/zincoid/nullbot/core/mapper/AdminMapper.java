package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.zincoid.nullbot.core.model.po.AdminPO;

@Mapper
public interface AdminMapper extends BaseMapper<AdminPO> {
}
