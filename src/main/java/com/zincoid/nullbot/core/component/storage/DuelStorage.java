package com.zincoid.nullbot.core.component.storage;

import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.information.DuelInfo;
import com.zincoid.nullbot.core.util.DuelUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuelStorage {

    private final Map<Long, DuelInfo> duels;
    private final String dataPath;

    public DuelStorage(StorageProperties storageProperties) {
        duels = new ConcurrentHashMap<>();
        dataPath = storageProperties.getResourcePath() + "/duel/data.csv";
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
