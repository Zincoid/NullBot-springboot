package org.bot.nullbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bot.nullbot.dao.po.UserPO;


@Mapper
public interface UserMapper extends BaseMapper<UserPO>
{
}
