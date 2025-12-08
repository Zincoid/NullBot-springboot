package org.bot.qqbot.dispatcher;

import org.bot.qqbot.annotation.CommandMapping;
import org.bot.qqbot.command.Command;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandRegistry
{
    private final Map<String, Command> commandMap = new HashMap<>();

    public CommandRegistry(ApplicationContext context)
    {
        Map<String, Command> beans = context.getBeansOfType(Command.class);
        for (Command command : beans.values()) {
            CommandMapping mapping = command.getClass().getAnnotation(CommandMapping.class);
            if (mapping != null) {
                for(String commandName : mapping.value()) {
                    commandMap.put(commandName, command);
                }
            }
        }
    }

    public Command getCommand(String commandName) {
        return commandMap.get(commandName);
    }
}