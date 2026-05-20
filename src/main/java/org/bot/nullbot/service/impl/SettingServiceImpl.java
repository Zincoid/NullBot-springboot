package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.setting.*;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService {

    private final SettingManager settingManager;

    // =================== 全局功能相关 ===================

    @Override
    public Setting get(Long groupId) { return settingManager.getSetting(groupId); }
    @Override
    public boolean set(Setting setting) { return settingManager.setSetting(setting); }

    // =================== WEB功能相关 ===================

    @Override
    public List<Setting> getAll() { return settingManager.getSettings(); }
    @Override
    public void setAll(List<Setting> settings) { settingManager.setSettings(settings); }

    // =================== BOT功能相关 ===================

    // ------------------- Limit 功能控制 --------------------

    @Override
    public LimitOption getLimitOption(Long groupId) { return get(groupId).getLimitOption(); }

    @Override
    public void setLimitOption(Long groupId, LimitOption limitOption) { get(groupId).setLimitOption(limitOption); }

    // ------------------- AI 功能控制 --------------------

    @Override
    public ChatOption getChatOption(Long groupId) { return get(groupId).getChatOption(); }

    @Override
    public void setChatOption(Long groupId, ChatOption chatOption) { get(groupId).setChatOption(chatOption); }

    // ----------------- Monitor 功能控制 -----------------

    @Override
    public MonitorOption getMonitorOption(Long groupId) { return get(groupId).getMonitorOption(); }

    @Override
    public void setMonitorOption(Long groupId, MonitorOption monitorOption) { get(groupId).setMonitorOption(monitorOption); }

    // ------------------ Guess 功能控制 ------------------

    @Override
    public GuessOption getGuessOption(Long groupId) { return get(groupId).getGuessOption(); }

    @Override
    public void setGuessOption(Long groupId, GuessOption guessOption) { get(groupId).setGuessOption(guessOption); }
}
