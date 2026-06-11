package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.service.system.SystemService;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Restart", "reboot", "重启"})
@Component
@RequiredArgsConstructor
public class RestartCmd implements Cmd {

    private final SystemService systemService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        String option = args.nextString();
        switch (option) {
            case "-app" -> {
                bot.sendGroupMsg(groupId, """
                        ⚠️重启指令已下发
                        - 模式: APP
                        - 将于3s后重启...""", false);
                log.info("☑ [Restart] 重启指令已下发 - Mode: APPLICATION");
                systemService.restart();
            }
            case "-jar" -> {
                bot.sendGroupMsg(groupId, """
                        ⚠️重启指令已下发
                        - 模式: JAR
                        - 将于3s后重启...""", false);
                log.info("☑ [Restart] 重启指令已下发 - Mode: JAR FILE");
                if (args.size() > 1) {
                    systemService.restartViaJar(args.nextFullString());
                } else {
                    systemService.restartViaJar();
                }
            }
            default -> throw new BotWarnException("无此模式");
        }
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Restart 命令
                功能: 重新启动应用
                限权: %d 级
                格式:
                1. Restart [-app]
                2. Restart [-jar] [可选: 路径]
                别名: reboot/重启""", getAccess()
        );
    }
}
