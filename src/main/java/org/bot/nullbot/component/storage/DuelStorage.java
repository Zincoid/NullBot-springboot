package org.bot.nullbot.component.storage;

import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.DuelInfo;
import org.bot.nullbot.util.DuelUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuelStorage
{
    private final Map<Long, DuelInfo> duels;
    private final String dataPath;

    public DuelStorage(FileStorageProperties fileStorageProperties) {
        duels = new ConcurrentHashMap<>();
        dataPath = fileStorageProperties.getResourcePath() + "/duel/data.csv";
    }

    public void initDuel(Long groupId) { duels.put(groupId, DuelUtil.getRandom(dataPath)); }
    public DuelInfo getDuel(Long groupId) {
        return duels.getOrDefault(groupId, null);
    }
    public void removeDuel(Long groupId) {
        duels.remove(groupId);
    }
}
