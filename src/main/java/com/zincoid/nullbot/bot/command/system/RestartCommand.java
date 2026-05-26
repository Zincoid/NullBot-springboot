package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.service.SystemService;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Restart", "reboot", "重启"})
@Component
@RequiredArgsConstructor
public class RestartCommand implements Command {

    private final SystemService systemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        String option = args.nextString();
        switch (option) {
            case "-app" -> {
                bot.sendGroupMsg(groupId, """
                        [重启] ⚠️指令已下发
                        - 模式: APPLICATION
                        - 将于3s后重启, 请稍候...""", false);
                log.info("☑ [Restart] APP 重启指令已下发");
                systemService.restart();
            }
            case "-jar" -> {
                bot.sendGroupMsg(groupId, """
                        [重启] ⚠️指令已下发
                        - 模式: JAR FILE
                        - 将于3s后重启, 请稍候...""", false);
                log.info("☑ [Restart] JAR 重启指令已下发");
                if (args.size() > 1) {
                    systemService.restartViaJar(args.nextFullString());
                } else {
                    systemService.restartViaJar();
                }
            }
            default -> throw new NullBotException("无此方式");
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
