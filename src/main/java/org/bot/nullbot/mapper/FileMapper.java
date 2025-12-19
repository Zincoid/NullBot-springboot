package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.entity.po.FilePO;

@Mapper
public interface FileMapper extends BaseMapper<FilePO>
{
}
