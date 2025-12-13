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

@CommandMapping({"ShowUserInfo", "展示用户信息", "用户信息"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ShowUserInfoCommand implements Command
{
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            UserPO user = userService.getUser(groupMessageEvent.getUserId());
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), user.toString(), false);
            log.info("\t\t\t\t├─[User.ShowInfo] 已获取用户信息 - {}", user.toString().replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[User.ShowInfo] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ShowUserInfo 命令\n功能: 展示用户信息\n限权: " + getAccess() + "\n格式: ShowUserInfo\n中文命令: 展示用户信息 或 用户信息";
    }
}
