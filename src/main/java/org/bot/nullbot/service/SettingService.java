package org.bot.nullbot.service;

import org.bot.nullbot.entity.info.SettingInfo;

public interface SettingService
{
    SettingInfo getSetting(Long groupId);

    boolean updateSetting(SettingInfo setting);
}
