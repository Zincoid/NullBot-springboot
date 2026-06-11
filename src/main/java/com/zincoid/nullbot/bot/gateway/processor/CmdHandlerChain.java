package com.zincoid.nullbot.bot.gateway.processor;

import com.mikuac.shiro.core.Bot;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.gateway.handler.Handler;

import java.util.Iterator;
import java.util.List;

public class CmdHandlerChain {

    private final Iterator<Handler> iterator;

    public CmdHandlerChain(List<Handler> handlers) {
        this.iterator = handlers.iterator();
    }

    public void doHandle(Bot bot, CmdEvent<?> event, Cmd cmd) throws Exception {
        if (iterator.hasNext()) {
            Handler next = iterator.next();
            next.handle(bot, cmd, event ,this);
        }
    }
}
