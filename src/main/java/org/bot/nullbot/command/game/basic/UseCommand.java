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
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.ItemService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@CommandMapping({"Use", "使用", "使用物品"})
@Component
@RequiredArgsConstructor
@Slf4j
public class UseCommand implements Command
{
    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            // 参数检查
            if (event.getCommandParameters().isEmpty())
                throw new NullBotMsgException("[使用] ❌参数不足");
            // 解析物品
            int itemId;
            try {
                itemId = Integer.parseInt(event.getCommandParameters().getFirst());
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[使用] ❌参数格式错误");
            }
            // 存在检查
            if (!itemService.exist(itemId))
                throw new NullBotMsgException("[使用] ❌该物品不存在");
            // 可用检查
            if (!itemService.isUsable(itemId))
                throw new NullBotMsgException("[使用] ❌该物品不可使用");
            // 库存检查
            Long userId = groupMessageEvent.getUserId();
            if (!inventoryService.decreaseInventory(userId, itemId, 1))
                throw new NullBotMsgException("[使用] ❌该物品数量不足");

            // 替换参数
            String command = itemService.getItemCommand(itemId);
            if ("UserBan".equals(command.split(" ")[0]))
                command = command.replace("userId", userId.toString());

            // 执行命令
            eventPublisher.publishEvent(new EmbeddedCommandEvent(bot, new CommandEvent<>(event.getEvent(), command, false, false)));

            // 发送通知
            String itemName = itemService.getItem(itemId).getName();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[使用] ✅" + userName + " 已使用 " + itemName + "！", false);
            log.info("\t\t\t\t├─[Use] 已使用");
        } else
            throw new NullBotLogException("[使用] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Use 命令
                功能: 使用库存的物品
                限权: %d 级
                格式: Use [物品ID]
                中文命令: 使用/使用物品""", getAccess()
        );
    }
}
