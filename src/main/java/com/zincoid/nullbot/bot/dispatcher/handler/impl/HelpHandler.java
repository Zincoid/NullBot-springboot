package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.model.bot.event.CommandEvent;
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
                log.info("├─[HelpHandler] 已输出群消息帮助");
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), command.getHelp(), false);
            } else if (event.getEvent() instanceof PrivateMessageEvent privateMessageEvent) {
                log.info("├─[HelpHandler] 私信暂无帮助功能");
                bot.sendPrivateMsg(privateMessageEvent.getUserId(), "⚠️私信暂无帮助功能", false);
            } else {
                log.info("├─[HelpHandler] 默认无帮助的事件");
            }
            return;
        }
        log.info("├─[HelpHandler] 非帮助命令");
        chain.doHandle(bot, event, command);
    }
}
