package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService
{
    private final SettingManager settingManager;

    @Override
    public SettingInfo getSetting(Long groupId) {
        return settingManager.getSetting(groupId);
    }

    @Override
    public boolean updateSetting(SettingInfo setting) {
        return settingManager.setSetting(setting);
    }
}
