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
    Integer cash;
    Integer capacity;
    Integer drawTimes;

    @Override
    public String toString() {
        return String.format(
                """
                        👤 用户信息
                        ├ 用户ID：%d
                        ├ 等级：%d
                        ├ 现金：%d
                        ├ 仓库容量：%d
                        └ 抽卡次数：%d""",
                id, level, cash, capacity, drawTimes
        );
    }
}
