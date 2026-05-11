package org.bot.nullbot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Order(1)
@Component
public class HelpHandler implements Handler {

    @Override
    public void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception {
        List<String> params = event.getCommandParameters();
        if (!params.isEmpty() && ("-help".equalsIgnoreCase(params.getFirst()) || "-h".equalsIgnoreCase(params.getFirst()))) {
            if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
                log.info("\t\t├─[HelpHandler] 已输出群消息帮助");
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), command.getHelp(), false);
            } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
                log.info("\t\t├─[HelpHandler] 暂无私信帮助功能");
                bot.sendPrivateMsg(privateMessageEvent.getUserId(), "[帮助] ⚠️暂无私信帮助功能", false);
            } else {
                log.info("\t\t├─[HelpHandler] 默认无帮助的事件");
            }
            return;
        }

        log.info("\t\t├─[HelpHandler] 非帮助命令");
        chain.doHandle(bot, event, command);
    }
}
