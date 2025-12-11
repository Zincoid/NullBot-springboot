package org.bot.nullbot.command.reply;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.stereotype.Component;

@CommandMapping({"Reply", "应答"})
@Component
@Slf4j
public class ReplyCommand implements Command
{
    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String reply = "无内容";
            if (!event.getCommandParameters().isEmpty())
                reply = event.getCommandParameters().getFirst();
            bot.sendGroupNotice(groupMessageEvent.getGroupId(), MsgUtils.builder().poke(groupMessageEvent.getUserId()).build());
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), reply, false);
            log.info("\t\t\t\t├─[Reply] 已回复 - {}", reply.replaceAll("\\R", ""));
        }else
            log.info("\t\t\t\t├─[Reply] 未设计 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ Reply 命令\n功能: 简单回复(子功能 不是给用户用的)\n限权: " + getAccess() + "\n格式: Reply [内容]\n中文命令: 应答";
    }
}
