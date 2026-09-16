package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.enums.setting.ChatScope;
import com.zincoid.nullbot.core.enums.setting.ChatStrategy;
import com.zincoid.nullbot.core.enums.setting.LimitScope;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SettingDTOTests {

    private static Validator validator;

    @BeforeAll
    static void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private SettingDTO valid() {
        SettingDTO dto = new SettingDTO();
        dto.setGroupId(123456L);
        dto.setLimitScope(LimitScope.USER);
        dto.setLimitCapacity(5);
        dto.setLimitRefill(2);
        dto.setLimitInterval(1);
        dto.setChatScope(ChatScope.GROUP);
        dto.setChatStrategy(ChatStrategy.EMBEDDING);
        dto.setThinking(false);
        dto.setVoice(false);
        dto.setVision(false);
        dto.setInnerCmdAuth(false);
        dto.setAntiInjection(true);
        dto.setCustom(false);
        dto.setAutoReply(false);
        dto.setReplyFrequency(0.001);
        dto.setImageCollect(false);
        dto.setMessageCollect(true);
        dto.setKeywordDetect(false);
        dto.setPokeDetect(true);
        dto.setRecallDetect(false);
        dto.setGuessCropRatio(0.1);
        dto.setGuessTransparentRatio(0.75);
        dto.setGuessPadding(250);
        return dto;
    }

    @Test
    void validDtoPasses() {
        assertTrue(validator.validate(valid()).isEmpty());
    }

    @Test
    void nullGroupIdRejected() {
        SettingDTO dto = valid();
        dto.setGroupId(null);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void replyFrequencyAboveOneRejected() {
        SettingDTO dto = valid();
        dto.setReplyFrequency(1.5);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void negativeCropRatioRejected() {
        SettingDTO dto = valid();
        dto.setGuessCropRatio(-0.1);
        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void toPoMapsAllFields() {
        SettingPO po = valid().toPo();
        assertTrue(123456L == po.getGroupId() && po.getLimitScope() == LimitScope.USER
                && po.getChatScope() == ChatScope.GROUP && po.getChatStrategy() == ChatStrategy.EMBEDDING
                && po.getLimitCapacity() == 5 && po.getReplyFrequency() == 0.001
                && po.isAntiInjection() && po.isMessageCollect() && po.isPokeDetect()
                && po.getGuessCropRatio() == 0.1 && po.getGuessTransparentRatio() == 0.75
                && po.getGuessPadding() == 250);
    }
}
