package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.result.PageResult;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.core.service.basic.InventoryService;
import com.zincoid.nullbot.core.service.basic.UserService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Inventory", "查看库存", "库存"})
@Component
@RequiredArgsConstructor
public class InventoryCommand implements Command {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        PageResult<InventoryVO> inventoryVOPage = inventoryService.getVOPage(userId, args.nextInt(1), 10);
        UserPO user = userService.getById(userId);
        int totalAmount = inventoryService.getTotalAmount(userId);
        StringBuilder sb = new StringBuilder()
                .append("[库存] ").append(userName).append("(").append(userId).append(")\n")
                .append("现金: ￥").append(user.getCash()).append("  容量: ").append(totalAmount).append("/").append(user.getCapacity()).append("\n")
                .append("[ID -- 名称 -- 品质/单价 - 数量]\n");
        if (inventoryVOPage.getTotal() > 0) {
            for (InventoryVO inventoryVO : inventoryVOPage.getData())
                sb.append(inventoryVO.toString()).append("\n");
        } else {
            sb.append("无物品...").append("\n");
        }
        sb.append("[第").append(inventoryVOPage.getCurrent()).append("页").append(" / 共").append(inventoryVOPage.getPages()).append("页 (每页").append(inventoryVOPage.getSize()).append("条)]");

        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("☑ [Inventory] 库存已获取 - UserId: {}", userId);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Inventory 命令
                功能: 查看库存物品
                限权: %d 级
                格式: Inventory [可选: 页码(默认为1)]
                别名: 查看库存/库存""", getAccess()
        );
    }
}
