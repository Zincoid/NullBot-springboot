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
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Spring] ✅重启指令已下发", false);
            log.info("\t\t\t\t├─[Spring] 重启指令已下发");
            systemService.restart();
        } else
            throw new NullBotLogException("[Spring] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Restart 命令
                功能: 重启应用
                限权: %d 级
                格式: Restart
                别名: 重启""", getAccess()
        );
    }
}
