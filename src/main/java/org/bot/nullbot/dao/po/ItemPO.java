package org.bot.nullbot.dao.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
     * 基础格式 - 纯文本
     */
    public String formatSimple() {
        return String.format(
                "【%s】 %s | 价格: %d | 稀有度: %s",
                name,
                truncate(description, 30),
                price,
                getRarityDisplay()
        );
    }

    /**
     * Markdown格式 - 适合支持Markdown的聊天软件
     */
    public String formatMarkdown() {
        String rarityEmoji = getRarityEmoji();
        String rarityColor = getRarityColorCode();

        return String.format(
                "**%s %s**  \n" +
                        "━━━━━━━━━━━━━━━━━━━━━━  \n" +
                        "▸ **稀有度:** %s%s%s  \n" +
                        "▸ **价格:** `%d` 金币  \n" +
                        "▸ **重量:** `%d`  \n" +
                        "▸ **描述:** %s  \n" +
                        "▸ **可用:** %s",
                rarityEmoji,
                name,
                rarityColor,
                getRarityDisplay(),
                getRarityColorCode(), // 用于结束颜色标记
                price,
                weight,
                truncate(description, 50),
                available ? "✅" : "❌"
        );
    }

    /**
     * Discord/QQ 风格 - 使用代码块
     */
    public String formatForDiscord() {
        return String.format(
                "```yaml\n" +
                        "物品: %s\n" +
                        "稀有度: %s %s\n" +
                        "价格: %,d 金币\n" +
                        "重量: %d\n" +
                        "ID: %d\n" +
                        "描述: %s\n" +
                        "```",
                name,
                getRarityDisplay(),
                getRarityStar(),
                price,
                weight,
                id,
                truncate(description, 40)
        );
    }

    /**
     * 表情符号增强版
     */
    public String formatWithEmoji() {
        return String.format(
                "🛒 **%s**  \n" +
                        "%s **稀有度:** %s  \n" +
                        "💰 **价格:** %,d 金币  \n" +
                        "⚖️ **重量:** %d  \n" +
                        "📝 **描述:** %s  \n" +
                        "%s **可用性:** %s",
                name,
                getRarityEmoji(),
                getRarityDisplay(),
                price,
                weight,
                truncate(description, 40),
                available ? "✅" : "❌",
                available ? "可获取" : "不可用"
        );
    }

    /**
     * 适合聊天软件的简洁格式（推荐）
     */
    public String formatForChat() {
        String rarityIcon = getRarityIcon();
        String statusIcon = available ? "🟢" : "🔴";

        return String.format(
                "%s【%s】%s  \n" +
                        "💎 稀有度: %s | 💰 价格: %,d | ⚖️ 重量: %d  \n" +
                        "📋 %s",
                rarityIcon,
                name,
                statusIcon,
                getRarityDisplay(),
                price,
                weight,
                truncate(description, 60)
        );
    }

    /**
     * 一行简洁格式
     */
    public String formatOneLine() {
        return String.format(
                "%s %s | 💰%,d | %s%s",
                getRarityIcon(),
                name,
                price,
                truncate(description, 25),
                available ? "" : " [不可用]"
        );
    }

    /**
     * 获取稀有度显示名称（带颜色）
     */
    private String getRarityDisplay() {
        switch (rarity) {
            case WHITE: return "白色";
            case GREEN: return "绿色";
            case BLUE: return "蓝色";
            case PURPLE: return "紫色";
            case GOLD: return "金色";
            case RED: return "红色";
            default: return "未知";
        }
    }

    /**
     * 获取稀有度对应的表情符号
     */
    private String getRarityEmoji() {
        switch (rarity) {
            case WHITE: return "⚪";
            case GREEN: return "🟢";
            case BLUE: return "🔵";
            case PURPLE: return "🟣";
            case GOLD: return "🟡";
            case RED: return "🔴";
            default: return "⚫";
        }
    }

    /**
     * 获取稀有度对应的小图标
     */
    private String getRarityIcon() {
        switch (rarity) {
            case WHITE: return "○";
            case GREEN: return "◇";
            case BLUE: return "◆";
            case PURPLE: return "♠";
            case GOLD: return "★";
            case RED: return "♥";
            default: return "•";
        }
    }

    /**
     * 获取稀有度星级表示
     */
    private String getRarityStar() {
        int level = rarity.ordinal() + 1;
        return "★".repeat(level) + "☆".repeat(5 - level);
    }

    /**
     * 获取稀有度颜色代码（Markdown格式）
     * 注意：不是所有聊天软件都支持HTML颜色
     */
    private String getRarityColorCode() {
        switch (rarity) {
            case WHITE: return ""; // 白色不需要颜色标记
            case GREEN: return "<font color='#00FF00'>";
            case BLUE: return "<font color='#0099FF'>";
            case PURPLE: return "<font color='#CC00FF'>";
            case GOLD: return "<font color='#FFD700'>";
            case RED: return "<font color='#FF3300'>";
            default: return "";
        }
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

    /**
     * 获取物品的详细信息（JSON格式，适合需要结构化的场景）
     */
    public String toJsonString() {
        return String.format(
                "{\"id\":%d,\"name\":\"%s\",\"rarity\":\"%s\",\"price\":%d,\"weight\":%d,\"description\":\"%s\"}",
                id, name, getRarityDisplay(), price, weight,
                description != null ? description.replace("\"", "\\\"") : ""
        );
    }

    /**
     * 获取物品链接格式（适合需要跳转的场景）
     */
    public String formatAsLink(String baseUrl) {
        return String.format(
                "[%s](%s/items/%d) - %s | 💰%,d 金币",
                name,
                baseUrl,
                id,
                getRarityDisplay(),
                price
        );
    }
}
