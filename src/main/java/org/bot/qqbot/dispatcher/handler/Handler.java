package org.bot.qqbot.dispatcher.handler;

import com.mikuac.shiro.core.Bot;
import org.bot.qqbot.command.Command;
import org.bot.qqbot.dispatcher.CommandHandlerChain;
import org.bot.qqbot.entity.CommandEvent;

public interface Handler {
    void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception;
}