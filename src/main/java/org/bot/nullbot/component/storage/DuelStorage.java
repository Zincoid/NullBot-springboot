package org.bot.nullbot.component.storage;

import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.DuelInfo;
import org.bot.nullbot.util.DuelUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuelStorage {

    private final Map<Long, DuelInfo> duels;
    private final String dataPath;

    public DuelStorage(FileStorageProperties fileStorageProperties) {
        duels = new ConcurrentHashMap<>();
        dataPath = fileStorageProperties.getResourcePath() + "/duel/data.csv";
    }

    public DuelInfo initDuel(Long groupId) {
        DuelInfo duel = DuelUtil.getRandom(dataPath);
        duels.put(groupId, duel);
        return duel;
    }

    public DuelInfo getDuel(Long groupId) {
        return duels.getOrDefault(groupId, null);
    }
    public DuelInfo removeDuel(Long groupId) { return duels.remove(groupId); }
}
