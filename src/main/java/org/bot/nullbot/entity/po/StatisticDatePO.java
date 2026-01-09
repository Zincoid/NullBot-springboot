package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`statistic_date`")
public class StatisticDatePO
{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private LocalDate date;
    private Long visits;
}
