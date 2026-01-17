package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.enums.Rarity;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Sell", "出售"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SellCommand implements Command
{
    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();

            if(params.isEmpty()) throw new NullBotMsgException("[出售] ❌参数不足");

            if("-r".equals(params.get(0))){
                if(params.size() < 2) throw new NullBotMsgException("[出售] ❌未指定稀有度");
                Rarity rarity;
                try {
                    rarity = Rarity.valueOf(params.get(1));
                } catch (IllegalArgumentException e) {
                    throw new NullBotMsgException("[出售] ❌稀有度参数错误");
                }
                if(!inventoryService.sellInventoryByRarity(userId, rarity))
                    throw new NullBotMsgException("[出售] ❌无该稀有度物品");
                UserPO user = userService.getUser(userId);
                bot.sendGroupMsg(groupId, "[出售] ✅已出售" + rarity.getDescription() + "色物品！\n" + "- 当前余额: " + user.getCash() + " ￥", false);
                log.info("\t\t\t\t├─[Sell] 按稀有度出售成功 - {}", rarity);
            } else {
                int itemId;
                int amount = 1;
                try {
                    itemId = Integer.parseInt(params.get(0));
                    if(params.size() >= 2){
                        amount = Integer.parseInt(params.get(1));
                        if(amount <= 0) throw new NullBotMsgException("[出售] ❌数量非正");
                    }
                } catch (NumberFormatException e) {
                    throw new NullBotMsgException("[出售] ❌参数格式错误");
                }
                if(!inventoryService.sellInventory(userId, itemId, amount))
                    throw new NullBotMsgException("[出售] ❌无该物品或数量不足");
                UserPO user = userService.getUser(userId);
                bot.sendGroupMsg(groupId, "[出售] ✅已出售！\n" + "- 当前余额: " + user.getCash() + " ￥", false);
                log.info("\t\t\t\t├─[Sell] 出售成功 - {} -> {}", itemId, amount);
            }
        } else
            throw new NullBotLogException("[出售] ❌未设计 - 非群消息事件响应方式");
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
