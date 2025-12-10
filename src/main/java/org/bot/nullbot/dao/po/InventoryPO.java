package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.enums.Rarity;

@Data
@AllArgsConstructor
public class InventoryPO
{
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    Long ownerId;
    Integer ItemId;
    String ItemName;
    Rarity rarity;
    Integer amount;

    @Override
    public String toString() {
        return ItemId + " " + rarity + " " + ItemName + " : " + amount;
    }
}
