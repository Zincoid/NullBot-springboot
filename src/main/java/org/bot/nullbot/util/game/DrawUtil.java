package org.bot.nullbot.util.game;

import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.enums.Rarity;

import java.util.List;
import java.util.Random;

public class DrawUtil
{
    private static final Random random = new Random();

    public static Rarity drawRarityByProbability() {
        double randomValue = random.nextDouble();
        double cumulativeProb = 0;
        for (Rarity rarity : Rarity.values()) {
            cumulativeProb += rarity.getProbability();
            if (randomValue < cumulativeProb) {
                return rarity;
            }
        }
        return Rarity.WHITE;
    }

    public static ItemPO drawItem(List<ItemPO> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }

    public static ItemPO drawItemByLogPrice(List<ItemPO> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        // 计算总权重（价格对数倒数）
        double totalWeight = 0.0;
        for (ItemPO item : items) {
            double price = item.getPrice() != null ? item.getPrice().doubleValue() : 0.0;
            // 使用对数：减小大价格的影响
            // 加1防止价格小于等于0的情况
            totalWeight += 1.0 / Math.log(price + Math.E);
        }
        // 随机选择
        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        for (ItemPO item : items) {
            double price = item.getPrice() != null ? item.getPrice().doubleValue() : 0.0;
            cumulativeWeight += 1.0 / Math.log(price + Math.E);
            if (randomValue < cumulativeWeight) {
                return item;
            }
        }
        return items.get(items.size() - 1);
    }
}
