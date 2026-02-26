package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SystemService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Restart", "reboot", "重启"})
@Component
@Slf4j
@RequiredArgsConstructor
public class RestartCommand implements Command
{
    private final SystemService systemService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty()) throw new NullBotMsgException("[重启] ❌未指定方式");
        Long groupId = event.getGroupId();
        String option = params.getFirst();
        switch (option)
        {
            case "-app" -> {
                bot.sendGroupMsg(groupId, """
                        [重启] ⚠️指令已下发
                        - 模式: APPLICATION
                        - 将于3s后重启, 请稍候...""", false);
                log.info("\t\t\t\t├─[Restart] APP 重启指令已下发");
                systemService.restart();
            }
            case "-jar" -> {
                bot.sendGroupMsg(groupId, """
                        [重启] ⚠️指令已下发
                        - 模式: JAR FILE
                        - 将于3s后重启, 请稍候...""", false);
                log.info("\t\t\t\t├─[Restart] JAR 重启指令已下发");
                try {
                    if (params.size() > 1)
                        systemService.restartViaJar(params.get(1));
                    else
                        systemService.restartViaJar();
                } catch (Exception e) {
                    throw new NullBotMsgException("[重启] ❌出错: " + e.getMessage());
                }
            }
            default -> throw new NullBotMsgException("[重启] ❌无此方式");
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
