package org.bot.nullbot.service;

import org.bot.nullbot.entity.info.SettingInfo;

import java.util.List;

public interface SettingService
{
    SettingInfo getSetting(Long groupId);

    boolean updateSetting(SettingInfo setting);

    List<SettingInfo> getSettingList();

    void addSettings(List<SettingInfo> settings);
}
