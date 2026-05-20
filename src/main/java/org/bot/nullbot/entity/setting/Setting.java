package org.bot.nullbot.entity.setting;

import lombok.Data;

@Data
public class Setting {

    private final Long groupId;

    private LimitOption limitOption;
    private ChatOption chatOption;
    private MonitorOption monitorOption;
    private GuessOption guessOption;

    public Setting(Long groupId) {
        this.groupId = groupId;
        this.limitOption = new LimitOption(groupId);
        this.chatOption = new ChatOption(groupId);
        this.monitorOption = new MonitorOption(groupId);
        this.guessOption = new GuessOption(groupId);
    }

    @Override
    public String toString() {
        return String.format("""
                 ◉ Limit 设置
                ├ 限速范围 - %s
                ├ 限速容量 - %s
                ├ 补充数量 - %s
                └ 补充间隔 - %s Min
                 ◉ AI 设置
                ├ 会话范围 - %s
                ├ 防注模式 - %s
                ├ 思考模式 - %s
                ├ 语音模式 - %s
                ├ 指令模式 - %s
                ├ 指令校验 - %s
                └ 自定模式 - %s
                ┌ 自动回复 - %s
                └ 回复频率 - %s
                 ◉ Monitor 设置
                ├ 图片收集 - %s
                ├ 消息收集 - %s
                ├ 词语检测 - %s
                ├ 戳戳检测 - %s
                └ 撤回检测 - %s
                 ◉ Guess 设置
                ├ 切割比例 - %s
                ├ 透明比例 - %s
                └ 切割边距 - %s""",
                limitOption.getLimitScope(),
                limitOption.getLimitCapacity(),
                limitOption.getLimitRefill(),
                limitOption.getLimitInterval(),
                chatOption.getChatScope(),
                chatOption.isAntiInjection() ? "ON" : "OFF",
                chatOption.isThinking() ? "ON" : "OFF",
                chatOption.isVoice() ? "ON" : "OFF",
                chatOption.isEmbedding() ? "ON" : "OFF",
                chatOption.isEmbeddingAuth() ? "ON" : "OFF",
                chatOption.isCustom() ? "ON" : "OFF",
                chatOption.isAutoReply() ? "ON" : "OFF",
                chatOption.getReplyFrequency(),
                monitorOption.isImageCollect() ? "ON" : "OFF",
                monitorOption.isMessageCollect() ? "ON" : "OFF",
                monitorOption.isKeywordDetect() ? "ON" : "OFF",
                monitorOption.isPokeDetect() ? "ON" : "OFF",
                monitorOption.isRecallDetect() ? "ON" : "OFF",
                guessOption.getGuessCropRatio(),
                guessOption.getGuessTransparentRatio(),
                guessOption.getGuessPadding()
        );
    }
}
