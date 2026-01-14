package org.bot.nullbot.service;

import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.enums.Scope;

import java.util.List;

public interface SettingService
{
    SettingInfo getSetting(Long groupId);

    boolean setSetting(SettingInfo setting);

    List<SettingInfo> getSettings();

    void setSettings(List<SettingInfo> settings);

    ChatOption getChatOption(Long groupId);

    boolean isAutoReply(Long groupId);

    double getReplyFrequency(Long groupId);

    Scope switchScope(Long groupId);

    boolean switchAntiInjection(Long groupId);

    boolean switchThinking(Long groupId);

    boolean switchEmbedding(Long groupId);

    boolean switchEmbeddingAuth(Long groupId);

    boolean switchCustom(Long groupId);

    boolean switchAutoReply(Long groupId);

    boolean setReplyFrequency(Long groupId, double frequency);

    boolean isImageCollect(Long groupId);

    boolean isMessageCollect(Long groupId);

    boolean isKeywordDetect(Long groupId);

    boolean isPokeDetect(Long groupId);

    boolean isRecallDetect(Long groupId);

    boolean switchImageCollect(Long groupId);

    boolean switchMessageCollect(Long groupId);

    boolean switchKeywordDetect(Long groupId);

    boolean switchPokeDetect(Long groupId);

    boolean switchRecallDetect(Long groupId);

    double getGuessRatio(Long groupId);

    int getGuessPadding(Long groupId);

    boolean setGuessParams(Long groupId, double ratio, int padding);
}
