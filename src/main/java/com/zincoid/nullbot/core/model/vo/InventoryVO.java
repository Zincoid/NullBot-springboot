package com.zincoid.nullbot.core.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.zincoid.nullbot.core.enums.Category;
import com.zincoid.nullbot.core.enums.Rarity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryVO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Long ownerId;
    private Integer itemId;
    private String itemName;
    private Category category;
    private Rarity rarity;
    private Integer price;
    private Integer amount;

    @Override
    public String toString() {
        return padRight(String.valueOf(itemId), 7) +
                padRight(itemName, 19) +
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
        return s + " ".repeat(need);
    }
}
