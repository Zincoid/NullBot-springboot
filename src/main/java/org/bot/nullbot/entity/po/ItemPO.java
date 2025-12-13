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
@TableName("item")
public class ItemPO
{
    @TableId(value = "id", type = IdType.AUTO)
    Integer id;
    String name;
    Rarity rarity;
    Category category;
    Integer price;
    Integer weight;
    String description;
    String imagePath;
    Boolean available;

    @Override
    public String toString() {
        return formatForChat();
    }

    /**
     * 适合聊天软件的简洁格式（推荐）
     */
    public String formatForChat() {
        String rarityIcon = getRarityEmoji();

        return String.format(
                """
                        %s【%s】
                        💎 品质: %s | 💰 价格: %,d | ⚖️ 重量: %d \s
                        📋 %s""",
                rarityIcon,
                name,
                getRarityDisplay(),
                price,
                weight,
                truncate(description, 60)
        );
    }

    /**
     * 获取稀有度显示名称（带颜色）
     */
    private String getRarityDisplay() {
        return switch (rarity) {
            case WHITE -> "白";
            case GREEN -> "绿";
            case BLUE -> "蓝";
            case PURPLE -> "紫";
            case GOLD -> "金";
            case RED -> "红";
            default -> "未知";
        };
    }

    /**
     * 获取稀有度对应的表情符号
     */
    private String getRarityEmoji() {
        return switch (rarity) {
            case WHITE -> "⚪";
            case GREEN -> "🟢";
            case BLUE -> "🔵";
            case PURPLE -> "🟣";
            case GOLD -> "🟡";
            case RED -> "🔴";
            default -> "⚫";
        };
    }

    /**
     * 获取稀有度星级表示
     */
    private String getRarityStar() {
        int level = rarity.ordinal() + 1;
        return "★".repeat(level) + "☆".repeat(5 - level);
    }

    /**
     * 截断字符串，超过长度加省略号
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}
