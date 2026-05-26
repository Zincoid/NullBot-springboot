package com.zincoid.nullbot.bot.command.game.single;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@CommandMapping({"Draw", "抽奖"})
@Component
@RequiredArgsConstructor
public class DrawCommand implements Command {

    private final ItemService itemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        if (args.isEmpty()) {
            ItemPO item = itemService.drawAndKeepRandom(userId);
            if (item == null)
                throw new BotWarnException("抽数耗尽或仓库已满");
            bot.sendGroupMsg(groupId, "[抽奖] " + userName + "抽到了...\n" + item, false);
            log.info("☑ [Draw] 物品已抽取 - {} -> {}", userId, item.getName());
        } else {
            int times = args.nextInt();
            if (times <= 0) throw new BotWarnException("次数非正");
            List<ItemPO> items = new ArrayList<>();
            boolean stop = false;
            while (times > 0 && !stop) {
                ItemPO item = itemService.drawAndKeepRandom(userId);
                if (item != null) {
                    items.add(item);
                    times--;
                } else {
                    stop = true;
                }
            }
            if (items.isEmpty())
                throw new BotWarnException("抽数耗尽或仓库已满");
            items.sort(Comparator.comparing(ItemPO::getRarity).reversed());
            StringBuilder sb = new StringBuilder("[抽奖] " + userName + "抽取了" + items.size() + "个物品...\n");
            for (ItemPO item : items)
                sb.append("[").append(item.getRarity().getDescription()).append(":").append(item.getName()).append("]");
            bot.sendGroupMsg(groupId, sb.toString(), false);
            log.info("☑ [Draw] 物品已抽取 - {} -> {}次", userId, items.size());
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Draw 命令
                功能: 抽奖 (可指定次数)
                限权: %d 级
                格式: Draw [可选: 次数]
                别名: 抽奖""", getAccess()
        );
    }
}
