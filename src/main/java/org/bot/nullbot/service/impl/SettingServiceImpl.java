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
public class SettingServiceImpl implements SettingService {

    private final SettingManager settingManager;

    // =================== 全局功能相关 ===================

    @Override
    public SettingInfo get(Long groupId) { return settingManager.getSetting(groupId); }
    @Override
    public boolean set(SettingInfo setting) { return settingManager.setSetting(setting); }

    // =================== WEB功能相关 ===================

    @Override
    public List<SettingInfo> getList() {
        return settingManager.getSettings();
    }
    @Override
    public void sets(List<SettingInfo> settings) { settingManager.setSettings(settings); }

    // =================== BOT功能相关 ===================

    // ------------------- Limit 功能控制 --------------------

    // 查询方法
    @Override
    public LimitScope getLimitScope(Long groupId) { return get(groupId).getLimitScope(); }
    @Override
    public int getLimitCapacity(Long groupId) { return get(groupId).getLimitCapacity(); }
    @Override
    public int getLimitRefill(Long groupId) { return get(groupId).getLimitRefill(); }
    @Override
    public int getLimitInterval(Long groupId) { return get(groupId).getLimitInterval(); }

    // 修改方法
    @Override
    public LimitScope switchLimitScope(Long groupId) { return get(groupId).switchLimitScope(); }

    @Override
    public boolean setLimitCapacity(Long groupId, int limitCapacity) {
        SettingInfo setting = get(groupId);
        setting.setLimitCapacity(limitCapacity);
        return true;
    }

    @Override
    public boolean setLimitRefill(Long groupId, int limitRefill) {
        SettingInfo setting = get(groupId);
        setting.setLimitRefill(limitRefill);
        return true;
    }

    @Override
    public boolean setLimitInterval(Long groupId, int limitInterval) {
        SettingInfo setting = get(groupId);
        setting.setLimitInterval(limitInterval);
        return true;
    }

    // ------------------- AI 功能控制 --------------------

    // 查询方法
    @Override
    public ChatOption getChatOption(Long groupId) { return settingManager.getChatOption(groupId); }
    @Override
    public boolean isAutoReply(Long groupId) { return get(groupId).isAutoReply(); }
    @Override
    public double getReplyFrequency(Long groupId) { return get(groupId).getReplyFrequency(); }

    // 修改方法
    @Override
    public ChatScope switchChatScope(Long groupId) { return get(groupId).switchChatScope(); }
    @Override
    public boolean switchAntiInjection(Long groupId) { return get(groupId).switchAntiInjection(); }
    @Override
    public boolean switchThinking(Long groupId) { return get(groupId).switchThinking(); }
    @Override
    public boolean switchVoice(Long groupId) {
        return get(groupId).switchVoice();
    }
    @Override
    public boolean switchEmbedding(Long groupId) { return get(groupId).switchEmbedding(); }
    @Override
    public boolean switchEmbeddingAuth(Long groupId) { return get(groupId).switchEmbeddingAuth(); }
    @Override
    public boolean switchCustom(Long groupId) { return get(groupId).switchCustom(); }
    @Override
    public boolean switchAutoReply(Long groupId) { return get(groupId).switchAutoReply(); }

    @Override
    public boolean setReplyFrequency(Long groupId, double frequency) {
        SettingInfo setting = get(groupId);
        setting.setReplyFrequency(frequency);
        return true;
    }

    // ----------------- Monitor 功能控制 -----------------

    // 查询方法
    @Override
    public boolean isImageCollect(Long groupId) { return get(groupId).isImageCollect(); }
    @Override
    public boolean isMessageCollect(Long groupId) { return get(groupId).isMessageCollect(); }
    @Override
    public boolean isKeywordDetect(Long groupId) { return get(groupId).isKeywordDetect(); }
    @Override
    public boolean isPokeDetect(Long groupId) { return get(groupId).isPokeDetect(); }
    @Override
    public boolean isRecallDetect(Long groupId) { return get(groupId).isRecallDetect(); }

    // 修改方法
    @Override
    public boolean switchImageCollect(Long groupId) { return get(groupId).switchImageCollect(); }
    @Override
    public boolean switchMessageCollect(Long groupId) { return get(groupId).switchMessageCollect(); }
    @Override
    public boolean switchKeywordDetect(Long groupId) { return get(groupId).switchKeywordDetect(); }
    @Override
    public boolean switchPokeDetect(Long groupId) { return get(groupId).switchPokeDetect(); }
    @Override
    public boolean switchRecallDetect(Long groupId) { return get(groupId).switchRecallDetect(); }

    // ------------------ Guess 功能控制 ------------------

    // 查询方法
    @Override
    public double getGuessCropRatio(Long groupId) { return get(groupId).getGuessCropRatio(); }
    @Override
    public double getGuessTransparentRatio(Long groupId) { return get(groupId).getGuessTransparentRatio(); }
    @Override
    public int getGuessPadding(Long groupId) {return get(groupId).getGuessPadding(); }

    // 修改方法
    @Override
    public boolean setGuessParams(Long groupId, double cropRatio, double transparentRatio, int padding) {
        SettingInfo setting = get(groupId);
        setting.setGuessCropRatio(cropRatio);
        setting.setGuessTransparentRatio(transparentRatio);
        setting.setGuessPadding(padding);
        return true;
    }
}
