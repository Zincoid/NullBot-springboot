package org.bot.nullbot.command.debug;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SystemService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Restart", "重启"})
@Component
@Slf4j
@RequiredArgsConstructor
public class RestartCommand implements Command
{
    private final SystemService systemService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[重启] ❌未指定方式");

            String option = params.getFirst();
            switch (option)
            {
                case "-APP" -> {
                    bot.sendGroupMsg(groupId, "[重启] ⚠️指令已下发\n- 方式: [-APP]\n- 请稍候...", false);
                    log.info("\t\t\t\t├─[Restart] APP重启指令已下发");
                    systemService.restart();
                }
                case "-JAR" -> {
                    bot.sendGroupMsg(groupId, "[重启] ⚠️指令已下发\n- 方式: [-JAR]\n- 请稍候...", false);
                    log.info("\t\t\t\t├─[Restart] JAR重启指令已下发");
                    try {
                        if (params.size() > 1)
                            systemService.restartViaJar(params.get(1));
                        else
                            systemService.restartViaJar();
                    } catch (Exception e) {
                        throw new  NullBotMsgException("[重启] ❌出错: " + e.getMessage());
                    }
                }
                default -> throw new NullBotMsgException("[重启] ❌无此方式");
            }
        } else
            throw new NullBotLogException("[重启] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Restart 命令
                功能: 重启应用
                限权: %d 级
                格式:
                1. Restart [-APP]
                2. Restart [-JAR] [可选: Path]
                别名: 重启""", getAccess()
        );
    }
}
