package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dao.po.ItemPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.ItemService;
import org.springframework.stereotype.Component;

@CommandMapping({"Draw", "抽奖"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DrawCommand implements Command
{
    private final ItemService itemService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long userId = groupMessageEvent.getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();
            ItemPO item = itemService.getAndKeepRandomItem(userId);
            if (item != null) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] " + userName + "抽到了...\n" + item, false);
                log.info("\t\t\t\t├─[Draw] 已抽取 - {} -> {}", userId, item.toString().replaceAll("\\R", ""));
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] ❌" + userName + "抽数已耗尽", false);
                log.info("\t\t\t\t├─[Draw] - {} -> 抽数已耗尽",  userId);
            }
        }else
            log.info("\t\t\t\t├─[Draw] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Draw 命令\n功能: 抽奖\n限权: " + getAccess() + "\n格式: Draw\n中文命令: 抽奖";
    }
}
