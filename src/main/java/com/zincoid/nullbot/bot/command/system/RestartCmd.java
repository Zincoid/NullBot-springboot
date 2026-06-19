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

    private static final String RESTART_MSG = """
                    ⚠️重启指令已下发
                    - 模式: %s
                    - 将于3s后重启...""";

    private final SystemService systemService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        if (args.hasOpt("app", "a")) {
            bot.sendGroupMsg(groupId, RESTART_MSG.formatted("APP"), false);
            log.info("☑ [Restart] 重启指令已下发 - Mode: APPLICATION");
            systemService.restart();
            return;
        }
        if (args.hasOpt("jar", "j")) {
            bot.sendGroupMsg(groupId, RESTART_MSG.formatted("JAR"), false);
            log.info("☑ [Restart] 重启指令已下发 - Mode: JAR FILE");
            if (!args.isEmpty())systemService.restartViaJar(args.rest());
            else systemService.restartViaJar();
            return;
        }
        throw new BotWarnException("无此模式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Restart 命令
                功能: 重新启动应用
                限权: %d 级
                用法: Restart [选项]

                选项:
                -a,--app        上下文重启
                -j,--jar [路径]  通过包重启

                别名: reboot/重启""", getAccess()
        );
    }
}
