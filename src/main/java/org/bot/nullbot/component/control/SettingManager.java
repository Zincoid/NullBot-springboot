package org.bot.nullbot.component.control;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.setting.Setting;
import org.bot.nullbot.util.CsvImportUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SettingManager {

    private final Map<Long, Setting> settings;

    public SettingManager(FileStorageProperties fileStorageProperties) {
        settings = new ConcurrentHashMap<>();
        try {
            List<Setting> defaultSettings = CsvImportUtil.importFromCsv(
                    fileStorageProperties.getConfigPath() + "/Settings.csv", Setting.class);
            setSettings(defaultSettings);
            log.info("▽ [SettingManager] 群组配置文件已载入");
        } catch (IOException e) {
            log.info("▽ [SettingManager] 群组配置文件未载入");
        }
    }

    public Setting getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new Setting(groupId));
    }

    public boolean setSetting(Setting setting) {
        return settings.put(setting.getGroupId(), setting) != null;
    }

    public List<Setting> getSettings() {
        return new ArrayList<>(settings.values());
    }

    public void setSettings(List<Setting> newSettings) {
        for (Setting setting : newSettings)
            settings.put(setting.getGroupId(), setting);
    }
}
