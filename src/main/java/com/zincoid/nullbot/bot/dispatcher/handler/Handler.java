package com.zincoid.nullbot.bot.dispatcher.handler;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.core.model.bot.CommandEvent;

public interface Handler {
    void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception;
}
