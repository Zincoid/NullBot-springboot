package org.bot.nullbot.service;

import org.bot.nullbot.entity.setting.*;

import java.util.List;

public interface SettingService {

    Setting get(Long groupId);

    boolean set(Setting setting);

    List<Setting> getAll();

    void setAll(List<Setting> settings);

    LimitOption getLimitOption(Long groupId);

    void setLimitOption(Long groupId, LimitOption limitOption);

    ChatOption getChatOption(Long groupId);

    void setChatOption(Long groupId, ChatOption chatOption);

    MonitorOption getMonitorOption(Long groupId);

    void setMonitorOption(Long groupId, MonitorOption monitorOption);

    GuessOption getGuessOption(Long groupId);

    void setGuessOption(Long groupId, GuessOption guessOption);
}
