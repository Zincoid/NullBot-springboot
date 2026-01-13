package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.service.GroupService;
import org.bot.nullbot.service.InventoryService;
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
    private final InventoryService inventoryService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            groupService.updateAllGroupNames();
            userService.updateAllUserNames();
            inventoryService.updateAllInventories();

            log.info("\t\t\t\t├─[DatabaseUpdate] 数据库已更新");
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[DatabaseUpdate] ✅已更新", false);
        }else
            throw new NullBotLogException("[数据库更新] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DatabaseUpdate 命令
                功能: 更新数据库
                限权: %d 级
                格式: DatabaseUpdate
                中文命令: 数据库更新""", getAccess()
        );
    }
}
