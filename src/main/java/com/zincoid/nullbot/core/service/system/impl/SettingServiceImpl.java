package com.zincoid.nullbot.core.service.system.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.core.component.control.SettingManager;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.service.system.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingManager settingManager;

    @Override
    public SettingPO get(Long groupId) {
        return settingManager.getSetting(groupId);
    }

    @Override
    public boolean set(SettingPO setting) {
        return settingManager.setSetting(setting);
    }

    @Override
    public List<SettingPO> getAll() {
        return settingManager.getSettings();
    }

    @Override
    public void setAll(List<SettingPO> settings) {
        settingManager.setSettings(settings);
    }
}
