package com.zincoid.nullbot.bot.dispatcher.handler.impl;

import com.mikuac.shiro.core.Bot;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.bot.dispatcher.handler.Handler;
import com.zincoid.nullbot.core.enums.EventScope;
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
            EventScope eventScope = event.getEventScope();
            if (eventScope == EventScope.GROUP) {
                log.info("├─[HelpHandler] 群聊帮助已输出");
                bot.sendGroupMsg(event.getGroupId(), command.getHelp(), false);
            } else if (eventScope == EventScope.PRIVATE) {
                log.info("├─[HelpHandler] 私聊帮助不可用");
                bot.sendPrivateMsg(event.getUserId(), "⚠️私聊暂无帮助", false);
            } else {
                log.info("├─[HelpHandler] 未知事件无帮助");
            }
            return;
        }
        log.info("├─[HelpHandler] 非帮助命令");
        chain.doHandle(bot, event, command);
    }
}
