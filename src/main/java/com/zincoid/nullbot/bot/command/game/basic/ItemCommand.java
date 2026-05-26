package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.ItemService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Item", "item", "物品", "查询物品"})
@Component
@RequiredArgsConstructor
public class ItemCommand implements Command {

    private final ItemService itemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        int itemId = args.nextInt();
        if (!itemService.exist(itemId))
            throw new BotWarnException("该物品不存在");
        ItemPO item = itemService.get(itemId);
        bot.sendGroupMsg(event.getGroupId(), item.toString(), false);
        log.info("☑ [Item] 物品详情已获取 - ItemId: {}", itemId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Item 命令
                功能: 查询物品详情
                限权: %d 级
                格式: Item [物品ID]
                别名: item/查询物品/物品""", getAccess()
        );
    }
}
