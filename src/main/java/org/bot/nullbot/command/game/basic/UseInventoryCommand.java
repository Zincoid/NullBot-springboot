package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.EmbeddedCommandEvent;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.ItemService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@CommandMapping({"UseInventory", "使用库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class UseInventoryCommand implements Command
{
    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            // 参数检查
            if (event.getCommandParameters().isEmpty()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌参数不足", false);
                log.info("\t\t\t\t├─[Inventory.Use] 参数不足");
                return;
            }

            // 解析物品
            Integer itemId;
            try {
                itemId = Integer.valueOf(event.getCommandParameters().getFirst());
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌参数格式错误", false);
                log.info("\t\t\t\t├─[Inventory.Use] 参数格式错误");
                return;
            }

            // 可用检查
            if (!itemService.isUsable(itemId)) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品不可使用", false);
                log.info("\t\t\t\t├─[Inventory.Use] 该物品不可使用");
                return;
            }

            ItemPO item = itemService.getItem(itemId);
            String command = itemService.getCommandFromItemDesc(itemId);

            // 命令检查
            if (command == null) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品暂未设计相关指令", false);
                log.info("\t\t\t\t├─[Inventory.Use] 该物品暂未设计相关指令");
                return;
            }

            Long userId = groupMessageEvent.getUserId();

            // 库存检查
            if (!inventoryService.decreaseInventory(userId, itemId)) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品数量不足", false);
                log.info("\t\t\t\t├─[Inventory.Use] 该物品数量不足");
                return;
            }

            // 替换参数
            if ("UserBan".equals(command.split(" ")[0])) {
                command = command.replace("userId", userId.toString());
            }

            // 执行命令
            eventPublisher.publishEvent(new EmbeddedCommandEvent(bot, new CommandEvent<>(event.getEvent(), command, false)));

            // 发送通知
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ✅" + userName + "已使用" + item.getName() + "！", false);
            log.info("\t\t\t\t├─[Inventory.Use] 已使用");
        } else {
            log.info("\t\t\t\t├─[UseItem.Use] 未设计 非群消息事件响应方式");
        }
    }

    @Override
    public String getHelp() {
        return "◉ UseInventory 命令\n功能: 使用库存物品\n限权: " + getAccess() + "\n格式: UseInventory [库存物品ID]\n中文命令: 使用库存";
    }
}
