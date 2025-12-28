package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("group")
public class GroupPO
{
    private Long  id;
    private Integer access;
}
