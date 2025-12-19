package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.page.InventoryPage;
import org.bot.nullbot.service.InventoryService;
import org.springframework.stereotype.Component;


@CommandMapping({"ShowInventory", "展示库存", "库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ShowInventoryCommand implements Command
{
    private final InventoryService inventoryService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            long p = 1;
            if(!event.getCommandParameters().isEmpty())
                try {
                    p = Integer.parseInt(event.getCommandParameters().getFirst());
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌页码格式错误", false);
                    log.info("\t\t\t\t├─[Inventory.Show] 页码格式错误");
                    return;
                }
            Long userId = groupMessageEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            InventoryPage inventoryPage = inventoryService.getInventoriesPage(userId, p, 8);
            StringBuilder sb = new StringBuilder().append("[库存] ").append(userName).append("(").append(userId).append(")\n").append("[ID -- 名称 -- 品质/单价 - 数量]");
            for(InventoryPO inventoryPO : inventoryPage.getInventories()) {
                sb.append("\n").append(inventoryPO.toString());
            }
            sb.append("\n").append("[第").append(inventoryPage.getCurrentPage()).append("页").append(" / 共").append(inventoryPage.getTotalPage()).append("页 (每页").append(inventoryPage.getPageSize()).append("条)]");
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
            log.info("\t\t\t\t├─[Inventory.Show] 已获取库存 - {}", sb.toString().replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[Inventory.Show] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ShowInventory 命令\n功能: 通过页码展示库存物品(默认第1页)\n限权: " + getAccess() + "\n格式: ShowInventory [可选: 页码]\n中文命令: 展示库存 或 库存";
    }
}
