package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.command.manage.UserBanCommand;
import org.bot.nullbot.dao.po.ItemPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.InventoryService;
import org.bot.nullbot.service.ItemService;
import org.springframework.stereotype.Component;

@CommandMapping({"UseInventory", "使用库存"})
@Component
@RequiredArgsConstructor
@Slf4j
public class UseInventoryCommand implements Command
{
    private final InventoryService inventoryService;
    private final ItemService itemService;
    private final UserBanCommand userBanCommand;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(!event.getCommandParameters().isEmpty()){
                try {
                    Integer itemId = Integer.valueOf(event.getCommandParameters().getFirst());
                    if(itemService.isUsable(itemId)){
                        ItemPO item = itemService.getItem(itemId);
                        Long userId = groupMessageEvent.getUserId();
                        String userName = bot.getStrangerInfo(userId, false).getData().getNickname();
                        String command = itemService.getCommandFromItemDesc(itemId);  // 冗余 暂时不想改
                        if(command != null){
                            if (inventoryService.decreaseInventory(groupMessageEvent.getUserId(), itemId)) {

                                // 根据情况替换参数
                                command = command.replace("userId", userId.toString());
                                CommandEvent<GroupMessageEvent> commandEvent = new CommandEvent<>(command);

                                if("UserBan".equals(commandEvent.getCommandType())){
                                    commandEvent.setEvent(groupMessageEvent);
                                    userBanCommand.execute(bot, commandEvent);
                                }else{
                                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌找不到指令！", false);
                                    log.info("\t\t\t\t├─[Inventory.Use] 找不到指令");
                                }

                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ✅" + userName + "已使用" + item.getName() + "！", false);
                                log.info("\t\t\t\t├─[Inventory.Use] 已使用");
                            }else{
                                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品数量不足", false);
                                log.info("\t\t\t\t├─[Inventory.Use] 该物品数量不足");
                            }
                        }else{
                            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品暂未设计相关指令", false);
                            log.info("\t\t\t\t├─[Inventory.Use] 该物品暂未设计相关指令");
                        }
                    }else{
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌该物品不可使用", false);
                        log.info("\t\t\t\t├─[Inventory.Use] 该物品不可使用");
                    }
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[Inventory.Use] 参数格式错误");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[库存] ❌参数不足", false);
                log.info("\t\t\t\t├─[Inventory.Use] 参数不足");
            }

        }else
            log.info("\t\t\t\t├─[UseItem.Use] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ UseInventory 命令\n功能: 使用库存物品\n限权: " + getAccess() + "\n格式: UseInventory [库存物品ID]\n中文命令: 使用库存";
    }
}
