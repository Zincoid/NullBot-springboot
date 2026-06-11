package com.zincoid.nullbot.bot.gateway.handler;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.gateway.processor.CommandHandlerChain;
import com.zincoid.nullbot.bot.gateway.processor.CommandEvent;

public interface Handler {

    void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception;
}
