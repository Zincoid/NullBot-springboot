package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.ItemService;
import org.springframework.stereotype.Component;

@CommandMapping({"Item", "item", "物品", "查询物品"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ItemCommand implements Command
{
    private final ItemService itemService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().isEmpty())
                throw new NullBotMsgException("[查询物品] ❌参数不足");

            int itemId;
            try {
                itemId = Integer.parseInt(event.getCommandParameters().getFirst());
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[查询物品] ❌参数格式错误");
            }

            if (!itemService.exist(itemId))
                throw new NullBotMsgException("[查询物品] ❌该物品不存在");

            ItemPO item = itemService.getItem(itemId);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), item.toString(), false);
            log.info("\t\t\t\t├─[Item] 已获取物品详情 - {}", item.getName());
        }else
            throw new NullBotLogException("[查询物品] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Item 命令
                功能: 查询物品详情
                限权: %d 级
                格式: Item [物品ID] 或 item [物品ID]
                中文命令: 查询物品/物品""", getAccess()
        );
    }
}
