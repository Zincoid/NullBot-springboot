package com.zincoid.nullbot.core.module.storage;

import com.zincoid.nullbot.core.model.information.DuelData;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.utils.DuelUtil;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DuelStorage {

    private final Map<Long, DuelData> duels;
    private final String dataPath;

    public DuelStorage(StorageProperties storageProperties) {
        duels = new ConcurrentHashMap<>();
        dataPath = storageProperties.getResourcePath() + "/duel/data.csv";
    }

    public DuelData initDuel(Long groupId) {
        DuelData duel = DuelUtil.getRandom(dataPath);
        duels.put(groupId, duel);
        return duel;
    }

    public DuelData getDuel(Long groupId) {
        return duels.getOrDefault(groupId, null);
    }
    public DuelData removeDuel(Long groupId) { return duels.remove(groupId); }
}
