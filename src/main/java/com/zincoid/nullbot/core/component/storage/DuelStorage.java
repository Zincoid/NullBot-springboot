package com.zincoid.nullbot.core.component.storage;

import com.zincoid.nullbot.core.config.prop.FileStorageProperties;
import com.zincoid.nullbot.core.entity.info.DuelInfo;
import com.zincoid.nullbot.core.util.DuelUtil;
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
