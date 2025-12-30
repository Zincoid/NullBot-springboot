package org.bot.nullbot.dispatcher;

import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

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

    public String getCommandHelpsForAI(Set<String> commandSet) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Command> entry : commandMap.entrySet()) {
            if(commandSet.contains(entry.getKey())){
                sb.append(entry.getValue().getHelpForAI()).append("\n");
            }
        }
        return sb.toString();
    }
}