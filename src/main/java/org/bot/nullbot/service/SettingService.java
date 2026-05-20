package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.Setting;

import java.util.List;

public interface SettingService {

    Setting get(Long groupId);

    boolean set(Setting setting);

    List<Setting> getAll();

    void setAll(List<Setting> settings);
}
