package com.zincoid.nullbot.bot.gateway.processor;

import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CmdRegistry {

    private final Map<String, Cmd> cmdMap = new HashMap<>();

    public CmdRegistry(List<Cmd> cmds) {
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

    public String getCmdHelpsForAI(Set<String> cmdSet) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Cmd> entry : cmdMap.entrySet()) {
            if (cmdSet.contains(entry.getKey())) {
                sb.append(entry.getValue().getHelpForAI()).append("\n");
            }
        }
        return sb.toString().trim();
    }
}
