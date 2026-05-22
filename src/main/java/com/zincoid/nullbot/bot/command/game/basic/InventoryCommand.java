package com.zincoid.nullbot.bot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.DataPage;
import com.zincoid.nullbot.core.model.data.po.UserPO;
import com.zincoid.nullbot.core.model.data.vo.InventoryVO;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.InventoryService;
import com.zincoid.nullbot.core.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;


@CommandMapping({"Inventory", "查看库存", "库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCommand implements Command {

    private final InventoryService inventoryService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        int p = 1;
        if(!params.isEmpty())
            try {
                p = Integer.parseInt(params.getFirst());
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[库存] ❌页码格式错误");
            }
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        DataPage<InventoryVO> inventoryVOPage = inventoryService.getVOPage(userId, p, 10);
        UserPO user = userService.get(userId);
        int totalAmount = inventoryService.getTotalAmount(userId);
        StringBuilder sb = new StringBuilder()
                .append("[库存] ").append(userName).append("(").append(userId).append(")\n")
                .append("现金: ￥").append(user.getCash()).append("  容量: ").append(totalAmount).append("/").append(user.getCapacity()).append("\n")
                .append("[ID -- 名称 -- 品质/单价 - 数量]\n");
        if(inventoryVOPage.getTotal() > 0){
            for(InventoryVO inventoryVO : inventoryVOPage.getData()) {
                sb.append(inventoryVO.toString()).append("\n");
            }
        }else{
            sb.append("无物品...").append("\n");
        }
        sb.append("[第").append(inventoryVOPage.getCurrent()).append("页").append(" / 共").append(inventoryVOPage.getPages()).append("页 (每页").append(inventoryVOPage.getSize()).append("条)]");
        bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
        log.info("\t\t\t\t├─[Inventory] 已获取库存 - {}({})", userName, userId);
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
