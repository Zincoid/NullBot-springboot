package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.DefaultConfig;
import org.bot.nullbot.entity.info.SettingInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SettingManager
{
    private final DefaultConfig defaultConfig;
    private final Map<Long, SettingInfo> settings = new ConcurrentHashMap<>();

    public SettingInfo getSetting(Long groupId) {
        return settings.computeIfAbsent(groupId, k -> new SettingInfo(groupId, defaultConfig));
    }

    public boolean switchImageCollect(Long groupId) { return getSetting(groupId).switchImageCollect(); }
    public boolean switchMessageCollect(Long groupId) { return getSetting(groupId).switchMessageCollect(); }
    public boolean switchKeywordDetect(Long groupId) { return getSetting(groupId).switchKeywordDetect(); }
    public boolean switchPokeDetect(Long groupId) { return getSetting(groupId).switchPokeDetect(); }
    public boolean switchRecallDetect(Long groupId) { return getSetting(groupId).switchRecallDetect(); }

    public boolean setGuessParams(Long groupId, double ratio, int padding) {
        SettingInfo setting = getSetting(groupId);
        setting.setGuessRatio(ratio);
        setting.setGuessPadding(padding);
        return true;
    }

    public double getGuessRatio(Long groupId) { return getSetting(groupId).getGuessRatio(); }
    public int getGuessPadding(Long groupId) {return getSetting(groupId).getGuessPadding(); }
}
