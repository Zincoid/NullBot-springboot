package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.handler.impl.PermissionHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Lock", "锁定"})
@Component
@RequiredArgsConstructor
@Slf4j
public class LockCommand implements Command
{
    private final PermissionHandler permissionHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        boolean locked = permissionHandler.switchInMaintenance();
        bot.sendGroupMsg(event.getGroupId(),
                "[锁定] " + (locked ? "\uD83D\uDD12系统已锁定" : "\uD83D\uDD13系统已解锁"),
                false
        );
        log.info("\t\t\t\t├─[Lock] 系统已{}", locked ? "锁定" : "解锁");
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
