package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(1)
@Component
@Slf4j
public class HelpHandler implements Handler
{
    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if (!params.isEmpty()) {
                if ("-help".equalsIgnoreCase(params.getFirst()) || "-h".equalsIgnoreCase(params.getFirst())) {
                    log.info("\t\t├─[HelpHandler] 已输出 详细帮助");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), command.getHelp(), false);
                    return;
                }
            }
            log.info("\t\t├─[HelpHandler] 非帮助命令");
            chain.doHandle(bot, event, command);
        } else {
            log.info("\t\t├─[HelpHandler] 默认无帮助的事件");
            chain.doHandle(bot, event, command);
        }
    }
}
