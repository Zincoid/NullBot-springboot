package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.UserService;
import org.springframework.stereotype.Component;

@CommandMapping({"DatabaseUpdate", "数据库更新"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseUpdateCommand implements Command
{
    private final GroupService groupService;
    private final UserService userService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupService.updateAllGroupNames();
            userService.updateAllUserNames();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[DatabaseUpdate] ✅已更新", false);
        }else
            log.info("\t\t\t\t├─[DatabaseUpdate] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return "◉ DatabaseUpdate 命令\n功能: 更新数据库\n限权: " + getAccess() + "\n格式: DatabaseUpdate\n中文命令: 数据库更新";
    }
}
