package com.zincoid.nullbot.bot.command.game.indie;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.service.base.InventoryService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@CmdMapping({"Draw", "抽奖"})
@Component
@RequiredArgsConstructor
public class DrawCmd implements Cmd {

    private final InventoryService inventoryService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        if (args.hasNext()) {
            int times = args.nextInt();
            if (times <= 0) throw new BotWarnException("次数非正");
            List<ItemPO> items = new ArrayList<>();
            boolean stop = false;
            while (times > 0 && !stop) {
                ItemPO item = inventoryService.draw(userId);
                if (item != null) {
                    items.add(item);
                    times--;
                } else {
                    stop = true;
                }
            }
            if (items.isEmpty()) throw new BotInfoException(Emoji.INFO, "抽数耗尽或仓库已满");
            items.sort(Comparator.comparing(ItemPO::getRarity).reversed());
            StringBuilder sb = new StringBuilder("\uD83C\uDF81%s抽取%s物品...".formatted(userName, items.size()));
            for (ItemPO item : items)
                sb.append("\n").append("- ").append(item.getRarity().getDescription()).append(":").append(item.getName());
            bot.sendGroupMsg(groupId, sb.toString(), false);
            log.info("☑ [Draw] 物品已抽取 - {} -> {}", userId, items.size());
            return;
        }

        ItemPO item = inventoryService.draw(userId);
        if (item == null) throw new BotInfoException(Emoji.INFO, "抽数耗尽或仓库已满");
        bot.sendGroupMsg(groupId, "\uD83C\uDF81%s抽到了...\n%s".formatted(userName, item), false);
        log.info("☑ [Draw] 物品已抽取 - {} -> {}", userId, item.getName());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Draw 命令
                功能: 随机抽奖
                限权: %d 级
                格式: Draw [可选: 次数]
                别名: 抽奖""", getAccess()
        );
    }
}
