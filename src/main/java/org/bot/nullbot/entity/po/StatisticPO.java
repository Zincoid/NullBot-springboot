package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("statistic")
public class StatisticPO
{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long groupId;
    private Long userId;
    private String userName;
    private String command;
    private Long visits;
}
