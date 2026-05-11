package org.bot.nullbot.component.control;

import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.prop.DefaultProperties;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;
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

    private final DefaultProperties defaultProperties;
    private final Map<Long, SettingInfo> settings;

    public SettingManager(DefaultProperties defaultProperties, FileStorageProperties fileStorageProperties) {
        this.defaultProperties = defaultProperties;
        settings = new ConcurrentHashMap<>();
        try {
            List<SettingInfo> defaultSettings = CsvImportUtil.importFromCsv(
                    fileStorageProperties.getConfigPath() + "/Settings.csv", SettingInfo.class);
            setSettings(defaultSettings);
            log.info("▽ [SettingManager] 群组配置文件已载入");
        } catch (IOException e) {
            log.info("▽ [SettingManager] 群组配置文件未载入");
        }
    }

    public ChatOption getChatOption(Long groupId) {
        return getSetting(groupId).getChatOption();
    }

    public SettingInfo getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new SettingInfo(groupId, defaultProperties));
    }

    public boolean setSetting(SettingInfo setting) {
        return settings.put(setting.getGroupId(), setting) != null;
    }

    public List<SettingInfo> getSettings() {
        return new ArrayList<>(settings.values());
    }

    public void setSettings(List<SettingInfo> newSettings) {
        for (SettingInfo setting : newSettings)
            settings.put(setting.getGroupId(), setting);
    }
}
