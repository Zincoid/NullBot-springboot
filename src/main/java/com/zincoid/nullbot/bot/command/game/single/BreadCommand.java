package com.zincoid.nullbot.bot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.po.ItemPO;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.service.game.BreadService;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.UserService;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@CommandMapping({"Bread", "面包", "\uD83C\uDF5E"})
@Component
@RequiredArgsConstructor
public class BreadCommand implements Command {

    private final Random random = new Random();

    private final UserService userService;
    private final InventoryService inventoryService;
    private final BreadService breadService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        switch (args.nextString()) {
            case "-buy", "b" -> buy(bot, userId, groupId, userName);
            case "-eat", "e" -> eat(bot, userId, userName, groupId);
            case "-rob", "r" -> rob(bot, event, groupId, userId, userName);
            case "-gift", "g" -> gift(bot, event, groupId, userId, userName);
            case "-look", "l" -> look(bot, groupId, userId, userName);
            default -> throw new BotWarnException("无此操作");
        }
    }

    private void buy(Bot bot, Long userId, Long groupId, String userName) {
        int cost = 500;  // 需支付的现金
        if (random.nextInt(100) >= 10) {  // 10% 概率获得特殊面包
            int i = breadService.buyBasic(userId, cost);
            if (i > 0) {
                bot.sendGroupMsg(groupId, "\uD83C\uDF5E%s花费%s￥买到%s面包".formatted(userName, cost, i), false);
                log.info("☑ [Bread::Buy] 已购买普通面包 - {} -> {}", userId, i);
                return;
            }
        } else {
            ItemPO bread = breadService.buySpecial(userId, cost);
            if (bread != null) {
                bot.sendGroupMsg(groupId, "\uD83C\uDF5E%s花费%s￥买到1特殊面包\n%s".formatted(userName, cost, bread), false);
                log.info("☑ [Bread::Buy] 已购买特殊面包 - {} -> {}", userId, bread.getName());
                return;
            }
        }
        throw new BotInfoException(Emoji.INFO, "库容或现金不足");
    }

    private void eat(Bot bot, Long userId, String userName, Long groupId) {
        int exp = 5;  // 单个面包经验值
        if (random.nextInt(100) >= 10) {  // 10% 概率吃到过期面包
            int[] res = breadService.eatBasic(userId, exp);
            int i = res[0];
            if (i > 0) {
                int j = res[1];
                StringBuilder sb = new StringBuilder();
                sb.append("\uD83C\uDF5E%s吃掉%s面包".formatted(userName, i));
                sb.append("\n- 获得%sExp...".formatted(exp * i));
                while (j-- > 0) sb.append("\n- LEVEL UP！");
                bot.sendGroupMsg(groupId, sb.toString(), false);
                log.info("☑ [Bread::Eat] 已吃面包 - {} -> {}", userId, i);
                return;
            }
        } else {
            if (breadService.eatRotten(userId)) {
                bot.sendGroupMsg(groupId, "\uD83C\uDF5E%s吃到烂面包\n- Exp清空了".formatted(userName), false);
                log.info("☑ [Bread::Eat] 已吃烂面包 - UserId: {}", userId);
                return;
            }
        }
        throw new BotInfoException(Emoji.INFO, "面包不足");
    }

    private void rob(Bot bot, GroupMessageEvent groupMessageEvent, Long groupId, Long userId, String userName) {
        List<Long> atUserIds = MsgParseUtil.extractAtNumbers(groupMessageEvent.getRawMessage());
        if (atUserIds.isEmpty()) throw new BotWarnException("未指定对象");
        long targetId = atUserIds.getFirst(); // 抢第一人
        String targetName = bot.getStrangerInfo(targetId, true).getData().getNickname();
        if (!userService.exist(targetId)) throw new BotInfoException(Emoji.WARN, "对方未注册");
        int i = breadService.transferBasic(targetId, userId);
        if (i == 0) throw new BotInfoException(Emoji.INFO, "对方面包不足");
        bot.sendGroupMsg(groupId, "\uD83C\uDF5E%s抢%s%s面包".formatted(userName, targetName, i), false);
        log.info("☑ [Bread::Rob] 已抢面包 - {} -> {}", targetId, i);
    }

    private void gift(Bot bot, GroupMessageEvent groupMessageEvent, Long groupId, Long userId, String userName) {
        List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(groupMessageEvent.getRawMessage());
        if (qqNumbers.isEmpty()) throw new BotWarnException("未指定对象");
        long targetId = qqNumbers.getFirst(); // 送第一人
        String targetName = bot.getStrangerInfo(targetId, true).getData().getNickname();
        if (!userService.exist(targetId)) throw new BotInfoException(Emoji.WARN, "对方未注册");
        int i = breadService.transferBasic(userId, targetId);
        if (i == 0) throw new BotInfoException(Emoji.INFO, "面包不足");
        bot.sendGroupMsg(groupId, "\uD83C\uDF5E%s送%s%s面包".formatted(userName, targetName, i), false);
        log.info("☑ [Bread::Gift] 已送面包 - {} -> {}个", targetId, i);
    }

    private void look(Bot bot, Long groupId, Long userId, String userName) {
        List<InventoryVO> inventoryVOS = breadService.getVOList(userId);
        UserPO user = userService.get(userId);
        int totalAmount = inventoryService.getTotalAmount(userId);
        StringBuilder sb = new StringBuilder()
                .append("[面包] ").append(userName).append("(").append(userId).append(")\n")
                .append("现金: ￥").append(user.getCash()).append("  容量: ").append(totalAmount).append("/").append(user.getCapacity()).append("\n")
                .append("[ID -- 名称 -- 品质/单价 - 数量]\n");
        if (!inventoryVOS.isEmpty()) {
            for (InventoryVO inventoryVO : inventoryVOS)
                sb.append(inventoryVO.toString()).append("\n");
        } else {
            sb.append("无面包...");
        }
        bot.sendGroupMsg(groupId, sb.toString().trim(), false);
        log.info("☑ [Bread::Look] 面包库存已获取 - UserId: {}", userId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Bread 命令
                功能: 面包小游戏(有特殊事件)
                限权: %d 级
                格式: Bread [操作符] [参数]
                操作:
                - 查面包 [l|-look]
                - 买面包 [b|-buy]
                - 吃面包 [e|-eat]
                - 抢面包 [r|-rob] [@用户]
                - 送面包 [g|-gift] [@用户]
                别名: 面包/\uD83C\uDF5E""", getAccess()
        );
    }
}
