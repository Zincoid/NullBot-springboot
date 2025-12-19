package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.entity.po.FilePO;

@Mapper
public interface FileMapper extends BaseMapper<FilePO>
{
    @Insert("insert into file(file_name, file_size, directory, location, is_dir) " +
            "value (#{fileName}, #{fileSize}, #{directory}, #{location}, #{isDir})")
    void addFile(FilePO file);

    @Delete("delete from file where locate(#{location}, location) != 0")
    void deleteFile(String location);
}
