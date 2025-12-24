package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("user")
public class UserPO
{
    private Long  id;
    // private Integer access;
    private Integer level;
    private Integer cash;
    private Integer capacity;
    private Integer drawTimes;

    @Override
    public String toString() {
        return String.format(
                """
                        ◉ 用户信息
                        ├ ID：%d
                        ├ 等级：%d
                        ├ 现金：%d
                        ├ 抽数：%d
                        └ 仓库容量：%d""",
                id, level, cash, drawTimes, capacity
        );
    }
}
