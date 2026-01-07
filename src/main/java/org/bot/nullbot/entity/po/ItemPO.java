package org.bot.nullbot.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.enums.Category;
import org.bot.nullbot.enums.Rarity;

@Data
@AllArgsConstructor
@TableName("`item`")
public class ItemPO
{
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private Rarity rarity;
    private Category category;
    private Integer price;
    private Integer weight;
    private String description;
    private String imagePath;
    private Boolean available;

    @Override
    public String toString() {
        return formatForChat();
    }

    public String formatForChat() {
        String rarityIcon = getRarityEmoji();
        return String.format(
                """
                        [%sID:%d - %s]
                        品质:%s | 价格:%,d | 重量:%d
                        📋 %s""",
                rarityIcon,
                id,
                name,
                getRarityDisplay(),
                price,
                weight,
                truncate(description, 60)
        );
    }

    private String getRarityDisplay() {
        return switch (rarity) {
            case WHITE -> "白";
            case GREEN -> "绿";
            case BLUE -> "蓝";
            case PURPLE -> "紫";
            case GOLD -> "金";
            case RED -> "红";
        };
    }

    private String getRarityEmoji() {
        return switch (rarity) {
            case WHITE -> "⚪";
            case GREEN -> "🟢";
            case BLUE -> "🔵";
            case PURPLE -> "🟣";
            case GOLD -> "🟡";
            case RED -> "🔴";
        };
    }

    private String getRarityStar() {
        int level = rarity.ordinal() + 1;
        return "★".repeat(level) + "☆".repeat(5 - level);
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
