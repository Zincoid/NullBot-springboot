package com.zincoid.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import com.zincoid.nullbot.component.control.SettingManager;
import com.zincoid.nullbot.entity.po.SettingPO;
import com.zincoid.nullbot.service.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingManager settingManager;

    // =================== 全局功能相关 ===================

    @Override
    public SettingPO get(Long groupId) { return settingManager.getSetting(groupId); }

    @Override
    public boolean set(SettingPO setting) { return settingManager.setSetting(setting); }

    // =================== WEB功能相关 ===================

    @Override
    public List<SettingPO> getAll() { return settingManager.getSettings(); }

    @Override
    public void setAll(List<SettingPO> settings) { settingManager.setSettings(settings); }
}
