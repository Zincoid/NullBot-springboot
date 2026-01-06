package org.bot.nullbot.command.game.basic;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.po.UserPO;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

@CommandMapping({"UserInfo", "info", "个人信息"})
@Component
@RequiredArgsConstructor
@Slf4j
public class UserInfoCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            UserPO user = userService.getUser(groupMessageEvent.getUserId());
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), user.toString(), false);
            log.info("\t\t\t\t├─[UserInfo] 已获取个人信息 - {}", user.toString().replaceAll("\\R", " "));
        }else
            log.info("\t\t\t\t├─[UserInfo] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ UserInfo 命令
                功能: 展示个人信息
                限权: %d 级
                格式: UserInfo 或 info
                中文命令: 个人信息""", getAccess()
        );
    }
}
