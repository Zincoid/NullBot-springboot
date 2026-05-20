package org.bot.nullbot.service;

import org.bot.nullbot.entity.po.SettingPO;

import java.util.List;

public interface SettingService {

    SettingPO get(Long groupId);

    boolean set(SettingPO setting);

    List<SettingPO> getAll();

    void setAll(List<SettingPO> settings);
}
