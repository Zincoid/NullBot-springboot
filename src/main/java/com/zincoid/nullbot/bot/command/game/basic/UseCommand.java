package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.bot.event.InnerCommandEvent;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.InventoryService;
import com.zincoid.nullbot.core.service.ItemService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Use", "使用物品", "使用"})
@Component
@RequiredArgsConstructor
@Slf4j
public class UseCommand implements Command {

    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        // 参数检查
        if (params.isEmpty())
            throw new NullBotMsgException("[使用] ❌参数不足");
        // 解析物品
        int itemId;
        try {
            itemId = Integer.parseInt(params.getFirst());
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
        Long userId = event.getUserId();
        if (!inventoryService.decrease(userId, itemId, 1))
            throw new NullBotMsgException("[使用] ❌该物品数量不足");

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
        log.info("├─[Use] 已使用");
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
