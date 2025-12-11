package org.bot.nullbot.dispatcher;

import com.mikuac.shiro.core.Bot;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.dispatcher.handler.Handler;
import org.bot.nullbot.entity.CommandEvent;

import java.util.Iterator;
import java.util.List;

public class CommandHandlerChain
{
    private final Iterator<Handler> iterator;
    public CommandHandlerChain(List<Handler> handlers) {
        this.iterator = handlers.iterator();
    }

    public void doHandle(Bot bot, CommandEvent<?> event, Command command) throws Exception {
        if (iterator.hasNext()) {
            Handler next = iterator.next();
            next.handle(bot, command, event ,this);
        }
    }
}