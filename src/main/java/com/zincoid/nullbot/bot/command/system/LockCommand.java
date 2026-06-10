package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.core.model.bot.args.CommandArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.handler.impl.PermissionHandler;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Lock", "锁定"})
@Component
@RequiredArgsConstructor
public class LockCommand implements Command {

    private final PermissionHandler permissionHandler;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        boolean locked = permissionHandler.switchInMaintenance();
        String response = locked ? "🔐系统已锁定" : "🔓系统已解锁";
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [Lock] 系统锁定状态已变更 - Locked: {}", locked);
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
