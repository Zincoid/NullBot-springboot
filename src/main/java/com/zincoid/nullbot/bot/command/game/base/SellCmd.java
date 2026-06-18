package com.zincoid.nullbot.bot.command.game.base;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.enums.Rarity;
import com.zincoid.nullbot.core.service.base.InventoryService;
import com.zincoid.nullbot.core.service.base.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Sell", "出售"})
@Component
@RequiredArgsConstructor
public class SellCmd implements Cmd {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();

        if ("--rarity".equals(args.getString(0)) || "-r".equals(args.getString(0))) {
            Rarity rarity = Rarity.valueOf(args.getString(1));
            if (!inventoryService.sell(userId, rarity))
                throw new BotInfoException(Emoji.INFO, "该稀有度物品不足");
            UserPO user = userService.getById(userId);
            bot.sendGroupMsg(groupId, "✅已出售%s色物品: 余额%s￥".formatted(rarity.getDescription(), user.getCash()), false);
            log.info("☑ [Sell] 出售成功 - Rarity: {}", rarity);
            return;
        }

        int itemId = args.getInt(0);
        int amount = args.getInt(1, 1);
        if (amount <= 0)
            throw new BotWarnException("数量非正");
        if (!inventoryService.sell(userId, itemId, amount))
            throw new BotInfoException(Emoji.INFO, "数量不足");
        UserPO user = userService.getById(userId);
        bot.sendGroupMsg(groupId, "✅已出售%s个物品: 余额%s￥".formatted(amount, user.getCash()), false);
        log.info("☑ [Sell] 出售成功 - {} -> {}", itemId, amount);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Sell 命令
                功能: 出售库存物品(可批量出售所有指定稀有度物品)
                限权: %d 级
                用法: Sell [选项] [物品ID] [数量]

                选项:
                  -r, --rarity   按稀有度批量出售

                稀有度: RED/GOLD/PURPLE/BLUE/GREEN/WHITE
                别名: 出售""", getAccess()
        );
    }
}
