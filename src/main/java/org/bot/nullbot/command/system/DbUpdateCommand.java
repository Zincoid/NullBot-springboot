package org.bot.nullbot.command.system;

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

@CommandMapping({"DbUpdate", "数据库更新"})
@Component
@RequiredArgsConstructor
@Slf4j
public class DbUpdateCommand implements Command
{
    private final GroupService groupService;
    private final UserService userService;
    private final InventoryService inventoryService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {

            // 自定义更新方式
            groupService.updateAllGroupNames();
            userService.updateAllUserNames();
            inventoryService.updateAllInventories();

            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[数据库更新] ✅已完成", false);
            log.info("\t\t\t\t├─[DbUpdate] 数据库已更新");
        }else
            throw new NullBotLogException("[数据库更新] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ DbUpdate 命令
                功能: 更新数据库条目
                限权: %d 级
                格式: DbUpdate
                别名: 数据库更新""", getAccess()
        );
    }
}
