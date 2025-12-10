package org.bot.nullbot.dao.po;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPO
{
    Long  id;
    Integer level;
    Integer drawTimes;
    Integer capacity;
}
