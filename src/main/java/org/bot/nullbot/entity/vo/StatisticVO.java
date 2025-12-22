package org.bot.nullbot.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatisticVO
{
    List<String> visitsXAxis;
    List<Long> visitsData;
}
