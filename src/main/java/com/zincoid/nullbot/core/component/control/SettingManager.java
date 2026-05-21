package com.zincoid.nullbot.core.component.control;

import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.po.SettingPO;
import com.zincoid.nullbot.core.util.CsvUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SettingManager {

    private final Map<Long, SettingPO> settings;

    public SettingManager(FileStorageProperties fileStorageProperties) {
        settings = new ConcurrentHashMap<>();
        try {
            List<SettingPO> defaultSettings = CsvUtil.importCsv(
                    fileStorageProperties.getConfigPath() + "/Settings.csv", SettingPO.class);
            setSettings(defaultSettings);
            log.info("▽ [SettingManager] 群组配置文件已载入");
        } catch (IOException e) {
            log.info("▽ [SettingManager] 群组配置文件未载入");
        }
    }

    public SettingPO getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new SettingPO(groupId));
    }

    public boolean setSetting(SettingPO setting) {
        return settings.put(setting.getGroupId(), setting) != null;
    }

    public List<SettingPO> getSettings() {
        return new ArrayList<>(settings.values());
    }

    public void setSettings(List<SettingPO> newSettings) {
        for (SettingPO setting : newSettings)
            settings.put(setting.getGroupId(), setting);
    }
}
