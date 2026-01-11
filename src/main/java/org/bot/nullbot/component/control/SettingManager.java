package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.DefaultConfig;
import org.bot.nullbot.entity.info.SettingInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SettingManager
{
    private final DefaultConfig defaultConfig;
    private final Map<Long, SettingInfo> settings = new ConcurrentHashMap<>();

    public SettingInfo getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new SettingInfo(groupId, defaultConfig));
    }
}
