package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.DefaultConfig;
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
    private final DefaultConfig defaultConfig;
    private final Map<Long, SettingInfo> settings = new ConcurrentHashMap<>();

    public ChatOption getChatOption(Long groupId) {
        SettingInfo setting = getSetting(groupId);
        return new ChatOption(setting.getScope(), setting.isAntiInjection(), setting.isThinking(),
                setting.isEmbedding(), setting.isEmbeddingAuth(), setting.isCustom());
    }

    public SettingInfo getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new SettingInfo(groupId, defaultConfig));
    }

    public boolean setSetting(SettingInfo setting) {
        return settings.put(setting.getGroupId(), setting) != null;
    }

    public List<SettingInfo> getSettings() {
        return new ArrayList<>(settings.values());
    }

    public void setSettings(List<SettingInfo> newSettings) {
        for (SettingInfo setting : newSettings) settings.put(setting.getGroupId(), setting);
    }
}
