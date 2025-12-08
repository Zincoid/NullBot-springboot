package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.component.control.AccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"AccessSet"})
@Component
@RequiredArgsConstructor
public class AccessSetCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(AccessSetCommand.class);
    private final AccessManager accessManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() >= 2){
                try {
                    long targetId = Long.parseLong(event.getCommandParameters().get(0));
                    int targetAccess = accessManager.getAccess(targetId);
                    int targetNewAccess = Integer.parseInt(event.getCommandParameters().get(1));
                    int selfAccess = accessManager.getAccess(groupMessageEvent.getUserId());
                    if(targetAccess >= selfAccess){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access.Set] 修改失败: 目标限权等级" + targetAccess + " 高于或等于 自身限权等级" + selfAccess, false);
                        logger.info("\t\t\t\t├─[Access.Set] 修改失败 - 目标限权等级{} 高于或等于 自身限权等级{}", targetAccess, selfAccess);
                    }else if(targetNewAccess >= selfAccess){
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access.Set] 修改失败: 新限权等级" + targetNewAccess + " 高于或等于 自身限权等级" + selfAccess, false);
                        logger.info("\t\t\t\t├─[Access.Set] 修改失败 - 新限权等级{} 高于或等于 自身限权等级{}", targetNewAccess, selfAccess);
                    }else{
                        accessManager.setAccess(targetId, targetNewAccess);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access.Set] 已修改用户 " + targetId + " 限权: " + targetAccess + " -> " + targetNewAccess, false);
                        logger.info("\t\t\t\t├─[Access.Set] 已修改用户 {} 限权 - {} -> {}", targetId, targetAccess, targetNewAccess);
                    }
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access.Set] 参数格式错误", false);
                    logger.info("\t\t\t\t├─[Access.Set] 参数格式错误");
                }
            }else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Access.Set] 参数不足", false);
                logger.info("\t\t\t\t├─[Access.Set] 参数不足");
            }
        }else
            logger.info("\t\t\t\t├─[Access.Set] 无 - 非群消息事件响应方式");
    }

    // @Override
    // public Integer getAccess() {
    //     return 1;
    // }

    @Override
    public String getHelp() {
        return "AccessSet 命令\n功能: 设置用户限权等级\n限权: 无法修改高于或等于自身限权用户 设置的限权无法高于或等于自身限权\n格式: AccessSet [QQ号] [限权等级]";
    }
}
