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
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;


@CommandMapping({"Inventory", "查看库存", "库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCommand implements Command
{
    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            long p = 1;
            if(!event.getCommandParameters().isEmpty())
                try {
                    p = Integer.parseInt(event.getCommandParameters().getFirst());
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌页码格式错误", false);
                    log.info("\t\t\t\t├─[Inventory] 页码格式错误");
                    return;
                }
            Long userId = groupMessageEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            InventoryPage inventoryPage = inventoryService.getInventoriesPage(userId, p, 10);
            UserPO user = userService.getUser(userId);
            int totalAmount = inventoryService.getTotalAmountByUserId(userId);
            StringBuilder sb = new StringBuilder()
                    .append("[库存] ").append(userName).append("(").append(userId).append(")\n")
                    .append("现金: ").append(user.getCash()).append(" ￥ 容量: ").append(totalAmount).append("/").append(user.getCapacity()).append("\n")
                    .append("[ID -- 名称 -- 品质/单价 - 数量]\n");
            if(inventoryPage.getTotal() > 0){
                for(InventoryPO inventoryPO : inventoryPage.getInventories()) {
                    sb.append(inventoryPO.toString()).append("\n");
                }
            }else{
                sb.append("无物品...").append("\n");
            }
            sb.append("[第").append(inventoryPage.getCurrentPage()).append("页").append(" / 共").append(inventoryPage.getTotalPage()).append("页 (每页").append(inventoryPage.getPageSize()).append("条)]");
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
            log.info("\t\t\t\t├─[Inventory] 已获取库存 - {}({})", userName, userId);
        }else
            log.info("\t\t\t\t├─[Inventory] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Inventory 命令
                功能: 查看库存物品
                限权: %d 级
                格式: Inventory [可选: 页码(默认为1)]
                中文命令: 查看库存/库存""", getAccess()
        );
    }
}
