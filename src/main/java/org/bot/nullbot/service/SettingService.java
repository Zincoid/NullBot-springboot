package org.bot.nullbot.service;

import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;

import java.util.List;

public interface SettingService
{
    SettingInfo getSetting(Long groupId);

    boolean setSetting(SettingInfo setting);

    ChatOption getChatOption(Long groupId);

    List<SettingInfo> getSettingList();

    void addSettings(List<SettingInfo> settings);
}
