package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("user")
public class UserPO
{
    Long  id;
    Integer level;
    Integer drawTimes;
    Integer capacity;
}
