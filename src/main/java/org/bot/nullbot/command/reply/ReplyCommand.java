package org.bot.nullbot.command.reply;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@CommandMapping({"Reply"})
@Component
public class ReplyCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ReplyCommand.class);

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String reply = "无内容";
            if (!event.getCommandParameters().isEmpty())
                reply = event.getCommandParameters().get(0);
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), reply, false);
            logger.info("\t\t\t\t├─[Reply] 已回复: {}", reply.replaceAll("\\R", ""));
        }else
            logger.info("\t\t\t\t├─[Reply] 未设计 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "/Reply 命令\n功能: 简单回复(子功能 不是给用户用的)\n限权: " + getAccess() + "\n格式: /Reply [内容]";
    }
}
