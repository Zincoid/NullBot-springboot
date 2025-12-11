package org.bot.nullbot.command;

import com.mikuac.shiro.core.Bot;
import org.bot.nullbot.entity.CommandEvent;

public interface Command
{
    void execute(Bot bot, CommandEvent<?> event) throws Exception;

    default String getHelp() { return "暂无帮助文档"; }

    default Integer getAccess() { return 0; };
}
