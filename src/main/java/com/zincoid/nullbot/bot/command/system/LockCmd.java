package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.gateway.handler.AuthHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Lock", "锁定"})
@Component
@RequiredArgsConstructor
public class LockCmd implements Cmd {

    private final AuthHandler authHandler;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        boolean locked = authHandler.switchInMaintenance();
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
                功能: 锁定系统
                限权: %d 级
                格式: Lock
                别名: 锁定""", getAccess()
        );
    }
}
