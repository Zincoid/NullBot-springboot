package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.po.ItemPO;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.ItemService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Item", "item", "物品", "查询物品"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemCommand implements Command {

    private final ItemService itemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[查询物品] ❌参数不足");

        int itemId;
        try {
            itemId = Integer.parseInt(params.getFirst());
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[查询物品] ❌参数格式错误");
        }

        if (!itemService.exist(itemId))
            throw new NullBotMsgException("[查询物品] ❌该物品不存在");

        ItemPO item = itemService.get(itemId);
        bot.sendGroupMsg(event.getGroupId(), item.toString(), false);
        log.info("\t\t\t\t├─[Item] 已获取物品详情 - {}", item.getName());
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
