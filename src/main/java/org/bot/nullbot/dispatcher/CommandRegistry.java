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
    private static final List<String> AI_COMMAND_BLACK_LIST = Arrays.asList(
            "Chat", "聊天",
            "PokeReact",
            "RecallReact"
    );

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

    public String getCommandSysMsg() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Command> entry : commandMap.entrySet()) {
            if(!AI_COMMAND_BLACK_LIST.contains(entry.getKey())){
                sb.append(entry.getValue().getHelp()).append("\n");
            }
        }
        return sb.toString();
    }
}