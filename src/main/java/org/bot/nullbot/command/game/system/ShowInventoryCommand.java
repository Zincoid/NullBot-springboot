package org.bot.nullbot.command.game.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dao.po.InventoryPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"ShowInventory", "展示库存", "库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ShowInventoryCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<InventoryPO> inventories = userService.getInventories(groupMessageEvent.getUserId());
            StringBuilder sb = new StringBuilder().append("物品ID-名称-稀有度-单价-数量");
            for(InventoryPO inventoryPO : inventories) {
                sb.append("\n").append(inventoryPO.toString());
            }
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
            log.info("\t\t\t\t├─[Inventory.Show] 已获取库存 - {}", sb.toString().replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[Inventory.Show] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ShowInventory 命令\n功能: 展示库存物品\n限权: " + getAccess() + "\n格式: ShowInventory\n中文命令: 展示库存 或 库存";
    }
}
