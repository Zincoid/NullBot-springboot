package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService
{
    private final SettingManager settingManager;

    // =================== 全局功能相关 ===================

    @Override
    public SettingInfo getSetting(Long groupId) {
        return settingManager.getSetting(groupId);
    }

    @Override
    public ChatOption getChatOption(Long groupId) { return settingManager.getChatOption(groupId); }

    @Override
    public boolean updateSetting(SettingInfo setting) {
        return settingManager.setSetting(setting);
    }

    // =================== WEB功能相关 ===================
    
    @Override
    public List<SettingInfo> getSettingList() {
        return settingManager.getSettingList();
    }

    @Override
    public void addSettings(List<SettingInfo> settings) { settingManager.setSettings(settings); }
}
