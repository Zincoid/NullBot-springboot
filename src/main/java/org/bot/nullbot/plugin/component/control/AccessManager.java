package org.bot.nullbot.plugin.component.control;

import org.bot.nullbot.config.FunctionDefaultConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessManager
{
    private final Map<Long, Integer> accesses;

    public AccessManager(FunctionDefaultConfig defaultConfig) { accesses = new ConcurrentHashMap<>(defaultConfig.getAccesses()); }

    public Integer getAccess(Long id) { return accesses.getOrDefault(id, 0); }

    public void setAccess(Long id, Integer access) { accesses.put(id, access); }
}
