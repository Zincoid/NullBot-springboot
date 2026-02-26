package org.bot.nullbot.service.impl;

import lombok.RequiredArgsConstructor;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.enums.ChatScope;
import org.bot.nullbot.enums.LimitScope;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl implements SettingService
{
    private final SettingManager settingManager;

    // =================== 全局功能相关 ===================

    @Override
    public SettingInfo getSetting(Long groupId) { return settingManager.getSetting(groupId); }
    @Override
    public boolean setSetting(SettingInfo setting) { return settingManager.setSetting(setting); }

    // =================== WEB功能相关 ===================

    @Override
    public List<SettingInfo> getSettings() {
        return settingManager.getSettings();
    }
    @Override
    public void setSettings(List<SettingInfo> settings) { settingManager.setSettings(settings); }

    // =================== BOT功能相关 ===================

    // ------------------- Limit 功能控制 --------------------

    // 查询方法
    @Override
    public LimitScope getLimitScope(Long groupId) { return getSetting(groupId).getLimitScope(); }
    @Override
    public int getLimitCapacity(Long groupId) { return getSetting(groupId).getLimitCapacity(); }
    @Override
    public int getLimitRefill(Long groupId) { return getSetting(groupId).getLimitRefill(); }
    @Override
    public int getLimitInterval(Long groupId) { return getSetting(groupId).getLimitInterval(); }

    // 修改方法
    @Override
    public LimitScope switchLimitScope(Long groupId) { return getSetting(groupId).switchLimitScope(); }

    @Override
    public boolean setLimitCapacity(Long groupId, int limitCapacity) {
        SettingInfo setting = getSetting(groupId);
        setting.setLimitCapacity(limitCapacity);
        return true;
    }

    @Override
    public boolean setLimitRefill(Long groupId, int limitRefill) {
        SettingInfo setting = getSetting(groupId);
        setting.setLimitRefill(limitRefill);
        return true;
    }

    @Override
    public boolean setLimitInterval(Long groupId, int limitInterval) {
        SettingInfo setting = getSetting(groupId);
        setting.setLimitInterval(limitInterval);
        return true;
    }

    // ------------------- AI 功能控制 --------------------

    // 查询方法
    @Override
    public ChatOption getChatOption(Long groupId) { return settingManager.getChatOption(groupId); }
    @Override
    public boolean isAutoReply(Long groupId) { return getSetting(groupId).isAutoReply(); }
    @Override
    public double getReplyFrequency(Long groupId) { return getSetting(groupId).getReplyFrequency(); }

    // 修改方法
    @Override
    public ChatScope switchChatScope(Long groupId) { return getSetting(groupId).switchChatScope(); }
    @Override
    public boolean switchAntiInjection(Long groupId) { return getSetting(groupId).switchAntiInjection(); }
    @Override
    public boolean switchThinking(Long groupId) { return getSetting(groupId).switchThinking(); }
    @Override
    public boolean switchVoice(Long groupId) {
        return getSetting(groupId).switchVoice();
    }
    @Override
    public boolean switchEmbedding(Long groupId) { return getSetting(groupId).switchEmbedding(); }
    @Override
    public boolean switchEmbeddingAuth(Long groupId) { return getSetting(groupId).switchEmbeddingAuth(); }
    @Override
    public boolean switchCustom(Long groupId) { return getSetting(groupId).switchCustom(); }
    @Override
    public boolean switchAutoReply(Long groupId) { return getSetting(groupId).switchAutoReply(); }

    @Override
    public boolean setReplyFrequency(Long groupId, double frequency) {
        SettingInfo setting = getSetting(groupId);
        setting.setReplyFrequency(frequency);
        return true;
    }

    // ----------------- Monitor 功能控制 -----------------

    // 查询方法
    @Override
    public boolean isImageCollect(Long groupId) { return getSetting(groupId).isImageCollect(); }
    @Override
    public boolean isMessageCollect(Long groupId) { return getSetting(groupId).isMessageCollect(); }
    @Override
    public boolean isKeywordDetect(Long groupId) { return getSetting(groupId).isKeywordDetect(); }
    @Override
    public boolean isPokeDetect(Long groupId) { return getSetting(groupId).isPokeDetect(); }
    @Override
    public boolean isRecallDetect(Long groupId) { return getSetting(groupId).isRecallDetect(); }

    // 修改方法
    @Override
    public boolean switchImageCollect(Long groupId) { return getSetting(groupId).switchImageCollect(); }
    @Override
    public boolean switchMessageCollect(Long groupId) { return getSetting(groupId).switchMessageCollect(); }
    @Override
    public boolean switchKeywordDetect(Long groupId) { return getSetting(groupId).switchKeywordDetect(); }
    @Override
    public boolean switchPokeDetect(Long groupId) { return getSetting(groupId).switchPokeDetect(); }
    @Override
    public boolean switchRecallDetect(Long groupId) { return getSetting(groupId).switchRecallDetect(); }

    // ------------------ Guess 功能控制 ------------------

    // 查询方法
    @Override
    public double getGuessCropRatio(Long groupId) { return getSetting(groupId).getGuessCropRatio(); }
    @Override
    public double getGuessTransparentRatio(Long groupId) { return getSetting(groupId).getGuessTransparentRatio(); }
    @Override
    public int getGuessPadding(Long groupId) {return getSetting(groupId).getGuessPadding(); }

    // 修改方法
    @Override
    public boolean setGuessParams(Long groupId, double cropRatio, double transparentRatio, int padding) {
        SettingInfo setting = getSetting(groupId);
        setting.setGuessCropRatio(cropRatio);
        setting.setGuessTransparentRatio(transparentRatio);
        setting.setGuessPadding(padding);
        return true;
    }
}
