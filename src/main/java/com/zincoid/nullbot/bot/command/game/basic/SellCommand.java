package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.service.InventoryService;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Sell", "出售"})
@Component
@RequiredArgsConstructor
public class SellCommand implements Command {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        if ("-r".equals(params.nextString())) {
            Rarity rarity = Rarity.valueOf(params.nextString());
            if (!inventoryService.sellByRarity(userId, rarity))
                throw new NullBotException("无该稀有度物品");
            UserPO user = userService.get(userId);
            bot.sendGroupMsg(groupId, "[出售] ✅已出售" + rarity.getDescription() + "色物品！\n" + "- 当前余额: " + user.getCash() + " ￥", false);
            log.info("☑ [Sell] 按稀有度出售成功 - Rarity: {}", rarity);
        } else {
            int itemId = params.nextInt();
            int amount = params.nextIntOptional(1);
            if (amount <= 0)
                throw new NullBotException("数量非正");
            if (!inventoryService.sell(userId, itemId, amount))
                throw new NullBotException("数量不足");
            UserPO user = userService.get(userId);
            bot.sendGroupMsg(groupId, "[出售] ✅已出售！\n" + "- 当前余额: " + user.getCash() + " ￥", false);
            log.info("☑ [Sell] 出售成功 - {} -> {}", itemId, amount);
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Sell 命令
                功能: 出售库存物品(可批量出售所有指定稀有度物品)
                限权: %d 级
                格式: Sell [物品ID] [可选: 数量(默认为1)]
                或 Sell -r [稀有度]
                稀有度: RED/GOLD/PURPLE/BLUE/GREEN/WHITE
                别名: 出售""", getAccess()
        );
    }
}
