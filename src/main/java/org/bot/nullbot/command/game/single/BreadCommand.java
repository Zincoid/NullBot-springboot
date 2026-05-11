package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.page.InventoryPage;
import org.bot.nullbot.entity.po.InventoryPO;
import org.bot.nullbot.entity.po.ItemPO;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.BreadService;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.UserService;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@CommandMapping({"Bread", "面包", "\uD83C\uDF5E"})
@Component
@RequiredArgsConstructor
@Slf4j
public class BreadCommand implements Command {

    private final UserService userService;
    private final InventoryService inventoryService;
    private final BreadService breadService;

    private final Random random = new Random();

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        if (params.isEmpty())
            throw new NullBotMsgException("[面包] ❌无操作");
        switch (params.getFirst()) {
            case "-buy", "b" -> buy(bot, userId, groupId, userName);
            case "-eat", "e" -> eat(bot, userId, userName, groupId);
            case "-rob", "r" -> rob(bot, event, groupId, userId, userName);
            case "-gift", "g" -> gift(bot, event, groupId, userId, userName);
            case "-look", "l" -> look(bot, params, groupId, userId, userName);
            default -> throw new NullBotMsgException("[面包] ❌操作不存在");
        }
    }

    private void buy(Bot bot, Long userId, Long groupId, String userName) {
        int cost = 500;  // 需支付的现金
        if (random.nextInt(100) >= 10) {  // 10% 概率获得特殊面包
            int i = breadService.buyBasicBread(userId, cost);
            if (i > 0) {
                bot.sendGroupMsg(groupId, userName + " 花费￥" + cost + "...\n- 买到" + i + "个面包！", false);
                log.info("\t\t\t\t├─[Bread-Buy] 已购买普通面包 - {}({}) -> {}个", userName, userId, i);
                return;
            }
        } else {
            ItemPO bread = breadService.buySpecialBread(userId, cost);
            if (bread != null) {
                bot.sendGroupMsg(groupId, userName + " 花费￥" + cost + "...\n- 买到1个特殊面包！\n" + bread, false);
                log.info("\t\t\t\t├─[Bread-Buy] 已购买特殊面包 - {}({}) -> {}", userName, userId, bread.getName());
                return;
            }
        }
        bot.sendGroupMsg(groupId, userName + " 库容或现金不足！", false);
        log.info("\t\t\t\t├─[Bread-Buy] 库容或现金不足");
    }

    private void eat(Bot bot, Long userId, String userName, Long groupId) {
        int exp = 5;  // 单个面包经验值
        if (random.nextInt(100) >= 10) {  // 10% 概率吃到过期面包
            int[] res = breadService.eatBasicBread(userId, exp);
            int i = res[0];
            if (i > 0) {
                int j = res[1];
                StringBuilder sb = new StringBuilder(userName + " 吃掉" + i + "个面包！\n- 获得 " + i * exp + "Exp！");
                while (j > 0) {
                    sb.append("\n- LEVEL UP！");
                    j--;
                }
                bot.sendGroupMsg(groupId, sb.toString(), false);
                log.info("\t\t\t\t├─[Bread-Eat] 已吃面包 - {}({}) -> {}个", userName, userId, i);
                return;
            }
        } else {
            if (breadService.eatRottenBread(userId)) {
                bot.sendGroupMsg(groupId, userName + " 吃到1个烂面包！\n- Exp清空了！", false);
                log.info("\t\t\t\t├─[Bread-Eat] 吃到烂面包 - {}({})", userName, userId);
                return;
            }
        }
        bot.sendGroupMsg(groupId, userName + " 面包没了！", false);
        log.info("\t\t\t\t├─[Bread-Buy] 普通面包不足");
    }

    private void rob(Bot bot, GroupMessageEvent groupMessageEvent, Long groupId, Long userId, String userName) {
        List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
        if (qqNumbers.isEmpty()) {
            bot.sendGroupMsg(groupId, "[抢面包] ❌未指定对象", false);
            log.info("\t\t\t\t├─[Bread-Rob] 未指定对象");
            return;
        }

        long targetId = qqNumbers.getFirst(); // 只抢第一个人
        String targetName = bot.getStrangerInfo(targetId, true).getData().getNickname();

        if (!userService.existUser(targetId)) {
            bot.sendGroupMsg(groupId, "[抢面包] ❌对象未注册", false);
            log.info("\t\t\t\t├─[Bread-Rob] 对象未注册 - {}({})", targetName, targetId);
            return;
        }

        int i = breadService.transferBasicBread(targetId, userId);
        if (i > 0) {
            bot.sendGroupMsg(groupId, userName + " 抢了 " + targetName + " " + i + "个面包！", false);
            log.info("\t\t\t\t├─[Bread-Rob] 已抢面包 - {}({}) -> {}个", targetName, targetId, i);
        } else {
            bot.sendGroupMsg(groupId, targetName + " 面包没了！", false);
            log.info("\t\t\t\t├─[Bread-Rob] 对方无面包 - {}({})", targetName, targetId);
        }
    }

    private void gift(Bot bot, GroupMessageEvent groupMessageEvent, Long groupId, Long userId, String userName) {
        List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
        if (qqNumbers.isEmpty()) {
            bot.sendGroupMsg(groupId, "[送面包] ❌未指定对象", false);
            log.info("\t\t\t\t├─[Bread-Gift] 未指定对象");
            return;
        }

        long targetId = qqNumbers.getFirst(); // 只送第一个人
        String targetName = bot.getStrangerInfo(targetId, true).getData().getNickname();

        if (!userService.existUser(targetId)) {
            bot.sendGroupMsg(groupId, "[送面包] ❌对象未注册", false);
            log.info("\t\t\t\t├─[Bread-Gift] 对象未注册 - {}({})", targetName, targetId);
            return;
        }

        int i = breadService.transferBasicBread(userId, targetId);
        if (i > 0) {
            bot.sendGroupMsg(groupId, userName + " 送了 " + targetName + " " + i + "个面包！", false);
            log.info("\t\t\t\t├─[Bread-Gift] 已送面包 - {}({}) -> {}个", targetName, targetId, i);
        } else {
            bot.sendGroupMsg(groupId, userName + " 面包没了！", false);
            log.info("\t\t\t\t├─[Bread-Gift] 自身无面包 - {}({})", userName, userId);
        }
    }

    private void look(Bot bot, List<String> params, Long groupId, Long userId, String userName) {
        int p = 1;
        if (params.size() > 1)
            try {
                p = Integer.parseInt(params.get(1));
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupId, "[查面包] ❌页码格式错误", false);
                log.info("\t\t\t\t├─[Bread-Look] 页码格式错误");
                return;
            }
        InventoryPage inventoryPage = breadService.getBreadPage(userId, p, 10);
        UserPO user = userService.getUser(userId);
        int totalAmount = inventoryService.getTotalAmountByUserId(userId);
        StringBuilder sb = new StringBuilder()
                .append("[面包] ").append(userName).append("(").append(userId).append(")\n")
                .append("现金: ￥").append(user.getCash()).append("  容量: ").append(totalAmount).append("/").append(user.getCapacity()).append("\n")
                .append("[ID -- 名称 -- 品质/单价 - 数量]\n");
        if (inventoryPage.getTotal() > 0) {
            for (InventoryPO inventoryPO : inventoryPage.getInventories())
                sb.append(inventoryPO.toString()).append("\n");
        } else {
            sb.append("无面包...").append("\n");
        }
        sb.append("[第").append(inventoryPage.getCurrentPage()).append("页").append(" / 共").append(inventoryPage.getTotalPage()).append("页 (每页").append(inventoryPage.getPageSize()).append("条)]");
        bot.sendGroupMsg(groupId, sb.toString(), false);
        log.info("\t\t\t\t├─[Bread-Look] 已获取面包库存 - {}({})", userName, userId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Bread 命令
                功能: 面包小游戏(有特殊事件)
                限权: %d 级
                格式: Bread [操作符] [参数]
                操作:
                - 查面包 [l|-look] [可选: 页码]
                - 买面包 [b|-buy]
                - 吃面包 [e|-eat]
                - 抢面包 [r|-rob] [@用户]
                - 送面包 [g|-gift] [@用户]
                别名: 面包/\uD83C\uDF5E""", getAccess()
        );
    }
}
