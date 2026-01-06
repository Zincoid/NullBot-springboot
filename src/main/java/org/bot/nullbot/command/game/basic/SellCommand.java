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

            if(params.isEmpty()){
                bot.sendGroupMsg(groupId, "[出售] ❌参数不足", false);
                log.info("\t\t\t\t├─[Sell] 参数不足");
                return;
            }

            if("-r".equals(params.get(0))){
                if(params.size() >= 2){
                    Rarity rarity;
                    try {
                        rarity = Rarity.valueOf(params.get(1));
                        if(inventoryService.sellInventoryByRarity(userId, rarity)){
                            bot.sendGroupMsg(groupId, "[出售] ✅已出售" + rarity.getDescription() + "色物品！", false);
                            log.info("\t\t\t\t├─[Sell] 按稀有度出售成功 - {}", rarity);
                        }else{
                            bot.sendGroupMsg(groupId, "[出售] ❌无该稀有度物品", false);
                            log.info("\t\t\t\t├─[Sell] 无该稀有度物品");
                        }
                    } catch (IllegalArgumentException e) {
                        bot.sendGroupMsg(groupId, "[出售] ❌稀有度参数错误", false);
                        log.info("\t\t\t\t├─[Sell] 稀有度参数错误");
                    }
                }else{
                    bot.sendGroupMsg(groupId, "[出售] ❌未指定稀有度", false);
                    log.info("\t\t\t\t├─[Sell] 未指定稀有度");
                }
                return;
            }

            int itemId;
            int amount = 1;
            try {
                itemId = Integer.parseInt(params.get(0));
                if(params.size() >= 2){
                    amount = Integer.parseInt(params.get(1));
                    if(amount <= 0){
                        bot.sendGroupMsg(groupId, "[出售] ❌数量非正", false);
                        log.info("\t\t\t\t├─[Sell] 数量非正");
                        return;
                    }
                }
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupId, "[出售] ❌参数格式错误", false);
                log.info("\t\t\t\t├─[Sell] 参数格式错误");
                return;
            }

            if(inventoryService.sellInventory(userId, itemId, amount)){
                UserPO user = userService.getUser(userId);
                bot.sendGroupMsg(groupId, "[出售] ✅已出售！\n" + "- 当前余额: " + user.getCash() + " ￥", false);
                log.info("\t\t\t\t├─[Sell] 出售成功");
            }else{
                bot.sendGroupMsg(groupId, "[出售] ❌无该物品或数量不足", false);
                log.info("\t\t\t\t├─[Sell] 无该物品或数量不足");
            }
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Sell 命令
                功能: 出售库存物品(可批量出售所有指定稀有度物品)
                限权: %d
                格式: Sell [物品ID] [可选: 数量(默认为1)]
                或 Sell -r [稀有度]
                稀有度: RED/GOLD/PURPLE/BLUE/GREEN/WHITE
                中文命令: 出售""", getAccess()
        );
    }
}
