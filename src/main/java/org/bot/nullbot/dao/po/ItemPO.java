package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.bot.nullbot.enums.Rarity;

@Data
public class ItemPO
{
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    String name;
    Rarity rarity;
    Integer price;
    Integer weight;
    String description;
    String imagePath;
    Boolean available;
}
