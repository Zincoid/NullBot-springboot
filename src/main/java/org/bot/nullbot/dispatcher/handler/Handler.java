package org.bot.nullbot.dispatcher.handler;

import com.mikuac.shiro.core.Bot;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.CommandHandlerChain;
import org.bot.nullbot.entity.CommandEvent;

public interface Handler {
    void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception;
}