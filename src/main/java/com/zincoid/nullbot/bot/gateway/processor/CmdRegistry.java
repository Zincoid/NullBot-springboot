package com.zincoid.nullbot.bot.gateway.processor;

import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.properties.bot.CmdProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CmdRegistry {

    private final CmdProperties cmdProperties;
    private final Map<String, Cmd> cmdMap;

    public CmdRegistry(
            CmdProperties cmdProperties,
            List<Cmd> cmds
    ) {
        this.cmdProperties = cmdProperties;
        if (!cmdProperties.isIgnoreCase()) cmdMap = new HashMap<>();
        else cmdMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Cmd cmd : cmds) {
            CmdMapping mapping = cmd.getClass().getAnnotation(CmdMapping.class);
            if (mapping != null) {
                for (String cmdName : mapping.value()) {
                    cmdMap.put(cmdName, cmd);
                }
            }
        }
    }

    public Cmd getCmd(String cmdName) {
        return cmdMap.get(cmdName);
    }

    @SafeVarargs
    public final boolean isCmdOf(String message, Class<? extends Cmd>... targetClasses) {
        if (message == null || !message.startsWith(cmdProperties.getPrefix())) return false;
        String cmdName = message.substring(cmdProperties.getPrefix().length()).trim().split("\\s+")[0];
        Cmd cmd = cmdMap.get(cmdName);
        if (cmd == null) return false;
        for (Class<? extends Cmd> targetClass : targetClasses) {
            if (targetClass.isInstance(cmd)) return true;
        }
        return false;
    }

    public String getCmdAIDoc(Set<String> cmds) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Cmd> entry : cmdMap.entrySet()) {
            if (cmds.contains(entry.getKey())) {
                sb.append(entry.getValue().getHelpForAI()).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
