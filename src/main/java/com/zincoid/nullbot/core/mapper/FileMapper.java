package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.zincoid.nullbot.core.model.data.po.FilePO;

@Mapper
public interface FileMapper extends BaseMapper<FilePO> {
}
