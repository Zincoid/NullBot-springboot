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

    @Override
    public String toString() {
        return String.format(
                """
                        👤 用户信息
                        ├ 用户ID：%d
                        ├ 用户等级：%d
                        ├ 抽卡次数：%d
                        └ 仓库容量：%d""",
                id, level, drawTimes, capacity
        );
    }
}
