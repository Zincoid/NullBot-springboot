package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.enums.Rarity;

@Data
@AllArgsConstructor
@TableName("inventory")
public class InventoryPO
{
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    Long ownerId;
    Integer ItemId;
    String ItemName;
    Rarity rarity;
    Integer price;
    Integer amount;

    @Override
    public String toString() {
        return padRight(String.valueOf(ItemId), 7) +
                padRight(ItemName, 19) +
                padRight(rarity.getDescription(), 5) +
                padRight(String.valueOf(price), 12) +
                padRight(String.valueOf(amount), 0);
    }

    private int getDisplayWidth(String s) {
        int width = 0;
        for (char c : s.toCharArray()) {
            width += (c <= 127 ? 2 : 3);
        }
        return width;
    }

    private String padRight(String s, int totalWidth) {
        int need = totalWidth - getDisplayWidth(s);
        if (need <= 0) return s;
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < need; i++) sb.append(' ');
        return sb.toString();
    }

}
