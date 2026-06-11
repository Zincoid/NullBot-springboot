package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.ItemService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Use", "使用物品", "使用"})
@Component
@RequiredArgsConstructor
public class UseCmd implements Cmd {

    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        int itemId = args.nextInt();

        if (!itemService.exist(itemId))
            throw new BotInfoException(Emoji.INFO, "物品不存在");
        if (!itemService.isUsable(itemId))
            throw new BotInfoException(Emoji.INFO, "物品不可用");
        if (!inventoryService.remove(userId, itemId, 1))
            throw new BotInfoException(Emoji.INFO, "物品数不足");

        String originalCmd = itemService.getCommand(itemId);
        String executeCmd = originalCmd.replace("userId", userId.toString());
        eventPublisher.publishEvent(CmdEvent.of(executeCmd, false));

        String itemName = itemService.getById(itemId).getName();
        bot.sendGroupMsg(event.getGroupId(), "✅%s已使用%s".formatted(userName, itemName), false);
        log.info("☑ [Use] 物品已使用 - {} -> {}", userId, itemId);
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
