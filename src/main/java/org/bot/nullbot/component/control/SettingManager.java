package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.prop.DefaultProperties;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SettingManager
{
    private final DefaultProperties defaultProperties;
    private final Map<Long, SettingInfo> settings = new ConcurrentHashMap<>();

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
