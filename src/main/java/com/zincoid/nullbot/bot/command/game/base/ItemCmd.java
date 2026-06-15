package com.zincoid.nullbot.bot.command.game.base;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.base.ItemService;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Item", "item", "物品", "查询物品"})
@Component
@RequiredArgsConstructor
public class ItemCmd implements Cmd {

    private final ItemService itemService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        int itemId = args.nextInt();
        if (!itemService.exist(itemId)) throw new BotInfoException(Emoji.INFO, "物品不存在");
        ItemPO item = itemService.getById(itemId);
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
