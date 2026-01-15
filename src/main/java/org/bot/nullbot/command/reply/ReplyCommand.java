package org.bot.nullbot.command.reply;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Reply", "应答"})
@Component
@Slf4j
public class ReplyCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (params.isEmpty()) throw new NullBotMsgException("[应答] ❌无参数");
            String message = String.join(" ", params.subList(0, params.size()));
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), message, false);
            log.info("\t\t\t\t├─[Reply] 已回复 - {}", message.replaceAll("\\R", " "));
        }else
            throw new NullBotLogException("[应答] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Reply 命令
                功能: 简单回复(废弃)
                限权: %d 级
                格式: Reply [内容]
                中文命令: 应答""", getAccess()
        );
    }
}
