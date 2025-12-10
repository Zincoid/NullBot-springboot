package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryPO
{
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    Long ownerId;
    Integer ItemId;
    String ItemName;
    Integer amount;

    @Override
    public String toString() {
        return ItemId + " " + ItemName + " : " + amount;
    }
}
