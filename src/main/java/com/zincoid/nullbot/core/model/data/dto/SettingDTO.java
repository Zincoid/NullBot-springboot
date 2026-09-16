package com.zincoid.nullbot.core.model.data.dto;

import com.zincoid.nullbot.core.enums.setting.ChatScope;
import com.zincoid.nullbot.core.enums.setting.ChatStrategy;
import com.zincoid.nullbot.core.enums.setting.LimitScope;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettingDTO {

    @NotNull(message = "群号不能为空")
    private Long groupId;

    @NotNull(message = "限速范围不能为空")
    private LimitScope limitScope;

    @Min(value = 0, message = "限速容量不能为负数")
    private int limitCapacity;

    @Min(value = 0, message = "补充数量不能为负数")
    private int limitRefill;

    @Min(value = 0, message = "补充间隔不能为负数")
    private int limitInterval;

    @NotNull(message = "会话范围不能为空")
    private ChatScope chatScope;

    @NotNull(message = "对话策略不能为空")
    private ChatStrategy chatStrategy;

    private boolean thinking;
    private boolean voice;
    private boolean vision;
    private boolean innerCmdAuth;
    private boolean antiInjection;
    private boolean custom;
    private boolean autoReply;

    @DecimalMin(value = "0.0", message = "回复频率不能为负数")
    @DecimalMax(value = "1.0", message = "回复频率不能大于 1")
    private double replyFrequency;

    private boolean imageCollect;
    private boolean messageCollect;
    private boolean keywordDetect;
    private boolean pokeDetect;
    private boolean recallDetect;

    @DecimalMin(value = "0.0", message = "切割比例不能为负数")
    @DecimalMax(value = "1.0", message = "切割比例不能大于 1")
    private double guessCropRatio;

    @DecimalMin(value = "0.0", message = "透明比例不能为负数")
    @DecimalMax(value = "1.0", message = "透明比例不能大于 1")
    private double guessTransparentRatio;

    @Min(value = 0, message = "切割边距不能为负数")
    private int guessPadding;
}
