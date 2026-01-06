package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.ItemService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
            if(event.getCommandParameters().isEmpty()){
                ItemPO item = itemService.getAndKeepRandomItem(userId);
                if (item != null) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] " + userName + "抽到了...\n" + item, false);
                    log.info("\t\t\t\t├─[Draw] 已抽取 - {} -> {}", userId, item.toString().replaceAll("\\R", " "));
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] ❌抽数耗尽或仓库已满", false);
                    log.info("\t\t\t\t├─[Draw] - {} -> 抽数(单抽)耗尽或仓库已满",  userId);
                }
            }else{
                try {
                    int times = Integer.parseInt(event.getCommandParameters().getFirst());
                    if(times <= 0){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "想干嘛...", false);
                        log.info("\t\t\t\t├─[Draw] 抽取次数非正");
                        return;
                    }
                    List<ItemPO> items = new ArrayList<>();
                    boolean stop = false;
                    while(times > 0 && !stop){
                        ItemPO item = itemService.getAndKeepRandomItem(userId);
                        if (item != null) {
                            items.add(item);
                            times--;
                        }else{
                            stop = true;
                        }
                    }
                    if (items.isEmpty()) {
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] ❌抽数耗尽或仓库已满", false);
                        log.info("\t\t\t\t├─[Draw] - {} -> 抽数(多抽)耗尽或仓库已满",  userId);
                    }else{
                        StringBuilder sb = new StringBuilder("[抽奖] " + userName + "抽取了" + items.size() + "个物品...\n");
                        for(ItemPO item : items){
                            sb.append("[").append(item.getRarity().getDescription()).append(":").append(item.getName()).append("]");
                        }
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), sb.toString(), false);
                        log.info("\t\t\t\t├─[Draw] 已抽取次数 - {} -> {}", userId, times);
                    }
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[抽奖] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[Draw] 参数格式错误");
                }
            }
        }else
            log.info("\t\t\t\t├─[Draw] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Draw 命令
                功能: 抽奖(可指定次数)
                限权: %d
                格式: Draw [可选: 次数]
                中文命令: 抽奖""", getAccess()
        );
    }
}
