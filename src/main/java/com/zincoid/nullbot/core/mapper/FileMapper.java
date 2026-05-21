package com.zincoid.nullbot.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.zincoid.nullbot.core.model.po.FilePO;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FilePO> {

    @Select("select * from file where file_name like concat('%',#{key},'%') and (directory = #{fullDir} or directory like concat(#{fullDir}, '/%'))")
    List<FilePO> searchFile(String key, String fullDir);

    @Select("select * from file where file_name like concat('%',#{key},'%') and (directory = #{fullDir} or directory like concat(#{fullDir}, '/%')) and visible = true")
    List<FilePO> searchFileVisible(String key, String fullDir);
}
