package com.zincoid.nullbot.core.component.ai.chat.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolRegistry {

    private final Map<String, Tool> toolMap = new LinkedHashMap<>();

    public void register(Tool tool) {
        toolMap.put(tool.getDef().getName(), tool);
    }

    public Tool get(String name) {
        return toolMap.get(name);
    }

    public List<ToolDef> getAll() {
        return toolMap.values().stream()
                .map(Tool::getDef)
                .toList();
    }
}
