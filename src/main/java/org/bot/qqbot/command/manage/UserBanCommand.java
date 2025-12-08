package org.bot.qqbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.qqbot.annotation.CommandMapping;
import org.bot.qqbot.command.Command;
import org.bot.qqbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"UserBan"})
@Component
public class UserBanCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(UserBanCommand.class);

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() >= 2){
                try {
                    long userId = Long.parseLong(event.getCommandParameters().get(0));
                    int time = Integer.parseInt(event.getCommandParameters().get(1));
                    bot.setGroupBan(groupMessageEvent.getGroupId(), userId, time * 60);
                    logger.info("\t\t\t\t├─[User.Ban] 已执行禁言 - {} -> {} min", userId, time);
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[User.Ban] 参数格式错误", false);
                    logger.info("\t\t\t\t├─[User.Ban] 参数格式错误");
                }
            }else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[User.Ban] 参数不足", false);
                logger.info("\t\t\t\t├─[User.Ban] 参数不足");
            }
        }else
            logger.info("\t\t\t\t├─[User.Ban] 无 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "/UserBan 命令\n功能: 用户禁言(时长设置为0则解除禁言)\n格式: /UserBan [QQ号] [时长(min)]";
    }
}
