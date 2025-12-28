package org.bot.nullbot.component.control;

import org.bot.nullbot.config.DefaultConfig;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
@Component
public class AccessManager
{
    private final Map<Long, Integer> userAccesses;
    private final Map<Long, Integer> groupAccesses;

    public AccessManager(DefaultConfig defaultConfig) {
        userAccesses = new ConcurrentHashMap<>(defaultConfig.getUserAccesses());
        groupAccesses = new ConcurrentHashMap<>(defaultConfig.getGroupAccesses());
    }

    public Integer getUserAccess(Long id) { return userAccesses.getOrDefault(id, 0); }
    public void setUserAccess(Long id, Integer access) { userAccesses.put(id, access); }

    public Integer getGroupAccess(Long id) { return groupAccesses.getOrDefault(id, 2); }
    public void setGroupAccess(Long id, Integer access) { groupAccesses.put(id, access); }
}
