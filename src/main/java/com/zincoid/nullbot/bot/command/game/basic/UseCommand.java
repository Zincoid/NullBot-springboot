package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.bot.event.InnerCommandEvent;
import com.zincoid.nullbot.core.service.InventoryService;
import com.zincoid.nullbot.core.service.ItemService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Use", "使用物品", "使用"})
@Component
@RequiredArgsConstructor
public class UseCommand implements Command {

    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        // 解析物品
        int itemId = args.nextInt();
        // 存在检查
        if (!itemService.exist(itemId))
            throw new BotWarnException("该物品不存在");
        // 可用检查
        if (!itemService.isUsable(itemId))
            throw new BotWarnException("物品不可使用");
        // 库存检查
        Long userId = event.getUserId();
        if (!inventoryService.decrease(userId, itemId, 1))
            throw new BotWarnException("物品数量不足");

        // 替换参数
        String command = itemService.getCommand(itemId);
        if ("UserBan".equals(command.split(" ")[0]))
            command = command.replace("userId", userId.toString());

        // 执行命令
        eventPublisher.publishEvent(InnerCommandEvent.of(command, false));

        // 发送通知
        String itemName = itemService.get(itemId).getName();
        String userName = event.getSender().getNickname();
        bot.sendGroupMsg(event.getGroupId(), "[使用] ✅" + userName + " 已使用 " + itemName + "！", false);
        log.info("☑ [Use] 物品已使用 - ItemId: {}", itemId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Use 命令
                功能: 使用库存的物品
                限权: %d 级
                格式: Use [物品ID]
                别名: 使用物品/使用""", getAccess()
        );
    }
}
