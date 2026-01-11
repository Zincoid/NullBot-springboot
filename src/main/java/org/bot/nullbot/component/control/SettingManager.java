package org.bot.nullbot.component.control;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.config.DefaultConfig;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.enums.Scope;
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

    public Scope switchScope(Long groupId) { return getSetting(groupId).switchScope(); }
    public boolean switchAntiInjection(Long groupId) { return getSetting(groupId).switchAntiInjection(); }
    public boolean switchThinking(Long groupId) { return getSetting(groupId).switchThinking(); }
    public boolean switchEmbedding(Long groupId) { return getSetting(groupId).switchEmbedding(); }
    public boolean switchEmbeddingAuth(Long groupId) { return getSetting(groupId).switchEmbeddingAuth(); }

    public Scope getScope(Long groupId) { return getSetting(groupId).getScope(); }
    public boolean isAntiInjection(Long groupId) { return getSetting(groupId).isAntiInjection(); }
    public boolean isThinking(Long groupId) { return getSetting(groupId).isThinking(); }
    public boolean isEmbedding(Long groupId) { return getSetting(groupId).isEmbedding(); }
    public boolean isEmbeddingAuth(Long groupId) { return getSetting(groupId).isEmbeddingAuth(); }

    public boolean switchImageCollect(Long groupId) { return getSetting(groupId).switchImageCollect(); }
    public boolean switchMessageCollect(Long groupId) { return getSetting(groupId).switchMessageCollect(); }
    public boolean switchKeywordDetect(Long groupId) { return getSetting(groupId).switchKeywordDetect(); }
    public boolean switchPokeDetect(Long groupId) { return getSetting(groupId).switchPokeDetect(); }
    public boolean switchRecallDetect(Long groupId) { return getSetting(groupId).switchRecallDetect(); }

    public boolean getImageCollect(Long groupId) { return getSetting(groupId).isImageCollect(); }
    public boolean getMessageCollect(Long groupId) { return getSetting(groupId).isMessageCollect(); }
    public boolean getKeywordDetect(Long groupId) { return getSetting(groupId).isKeywordDetect(); }
    public boolean getPokeDetect(Long groupId) { return getSetting(groupId).isPokeDetect(); }
    public boolean getRecallDetect(Long groupId) { return getSetting(groupId).isRecallDetect(); }

    public boolean setGuessParams(Long groupId, double ratio, int padding) {
        SettingInfo setting = getSetting(groupId);
        setting.setGuessRatio(ratio);
        setting.setGuessPadding(padding);
        return true;
    }
    public double getGuessRatio(Long groupId) { return getSetting(groupId).getGuessRatio(); }
    public int getGuessPadding(Long groupId) {return getSetting(groupId).getGuessPadding(); }
}
