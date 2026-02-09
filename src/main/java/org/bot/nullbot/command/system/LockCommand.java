package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.handler.impl.PermissionHandler;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.springframework.stereotype.Component;

@CommandMapping({"Lock", "锁定"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LockCommand implements Command
{
    private final PermissionHandler permissionHandler;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            boolean locked = permissionHandler.switchInMaintenance();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(),
                    "[锁定] " + (locked ? "\uD83D\uDD12系统已锁定" : "\uD83D\uDD13系统已解锁"),
                    false
            );
            log.info("\t\t\t\t├─[Lock] 系统已{}", locked ? "锁定" : "解锁");
        }else
            throw new NullBotLogException("[锁定] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Lock 命令
                功能: 锁定 NullBot 系统
                限权: %d 级
                格式: Lock
                别名: 锁定""", getAccess()
        );
    }
}
