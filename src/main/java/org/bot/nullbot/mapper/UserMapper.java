package org.bot.nullbot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.entity.po.UserPO;


@Mapper
public interface UserMapper extends BaseMapper<UserPO>
{
}
