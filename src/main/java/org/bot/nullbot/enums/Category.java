package org.bot.nullbot.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum Category
{
    COMMON(0, "基本物品"),
    USABLE(1, "可使用物品");

    @EnumValue
    private final int category;
    private final String description;

    Category(int category, String description) {
        this.category = category;
        this.description = description;
    }
}