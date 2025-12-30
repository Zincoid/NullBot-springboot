package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"UserBan", "禁言"})
@Component
@Slf4j
public class UserBanCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if (event.getCommandParameters().size() >= 2){
                try {
                    long userId = Long.parseLong(event.getCommandParameters().get(0));
                    int time = Integer.parseInt(event.getCommandParameters().get(1));
                    bot.setGroupBan(groupMessageEvent.getGroupId(), userId, time * 60);
                    log.info("\t\t\t\t├─[User.Ban] 已执行禁言 - {} -> {} min", userId, time);
                } catch (NumberFormatException e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[用户禁言] ❌参数格式错误", false);
                    log.info("\t\t\t\t├─[User.Ban] 参数格式错误");
                }
            }else {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[用户禁言] ❌参数不足", false);
                log.info("\t\t\t\t├─[User.Ban] 参数不足");
            }
        }else
            log.info("\t\t\t\t├─[User.Ban] 无 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ UserBan 命令
                功能: 用户禁言(时长设置为0则解除禁言)
                限权: %d
                格式: UserBan [QQ号] [时长(分钟)]
                中文命令: 禁言""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ UserBan 命令
                功能: 用户禁言(时长设置为0则解除禁言) 只有Zincoid可以调用！！！
                限权: %d
                格式: UserBan [QQ号] [时长(分钟)]""", getAccess()
        );
    }
}
