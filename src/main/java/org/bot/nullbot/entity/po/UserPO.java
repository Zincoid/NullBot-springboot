package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("`user`")
public class UserPO
{
    private Long  id;
    private String name;
    private Integer access;

    private Integer level;
    private Integer experience;
    private Integer cash;
    private Integer capacity;
    private Integer drawTimes;

    @Override
    public String toString() {
        return String.format(
                """
                        ◉ 个人信息
                        ├ QQ号：%d
                        ├ 昵称：%s
                        ├ 限权：%d 级
                        ├ 等级：Lv.%d Exp[%d/100]
                        ├ 现金：%d ￥
                        ├ 抽数：%d
                        └ 仓库容量：%d""",
                id, name, access, level, experience, cash, drawTimes, capacity
        );
    }
}
