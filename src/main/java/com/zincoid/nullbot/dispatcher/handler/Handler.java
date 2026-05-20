package com.zincoid.nullbot.dispatcher.handler;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.command.Command;
import com.zincoid.nullbot.dispatcher.CommandHandlerChain;
import com.zincoid.nullbot.entity.CommandEvent;

public interface Handler {
    void handle(Bot bot, Command command, CommandEvent<?> event, CommandHandlerChain chain) throws Exception;
}
