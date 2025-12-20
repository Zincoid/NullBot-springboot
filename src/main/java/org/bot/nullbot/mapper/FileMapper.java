package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.bot.nullbot.entity.po.FilePO;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FilePO>
{
    @Select("select * from file where file_name like concat('%',#{key},'%') and locate(#{fullDir}, directory) != 0")
    List<FilePO> searchFile(String key, String fullDir);
}
