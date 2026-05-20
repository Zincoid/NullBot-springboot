package org.bot.nullbot.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.enums.ChatScope;
import org.bot.nullbot.enums.LimitScope;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class SettingPO {

    private final Long groupId;

    private LimitScope limitScope = LimitScope.Group;
    private int limitCapacity = 25;
    private int limitRefill = 10;
    private int limitInterval = 1;

    private ChatScope chatScope = ChatScope.Group;
    private boolean antiInjection = true;
    private boolean thinking = false;
    private boolean voice = false;
    private boolean embedding = true;
    private boolean embeddingAuth = false;
    private boolean custom = false;
    private boolean autoReply = false;
    private double replyFrequency = 0.001;

    private boolean imageCollect = false;
    private boolean messageCollect = true;
    private boolean keywordDetect = true;
    private boolean pokeDetect = true;
    private boolean recallDetect = false;

    private double guessCropRatio = 0.1;
    private double guessTransparentRatio = 0.75;
    private int guessPadding = 250;

    public LimitScope switchLimitScope() { return this.limitScope = this.limitScope.next(); }

    public ChatScope switchChatScope() { return chatScope = chatScope.next(); }
    public boolean switchAntiInjection() { return antiInjection = !antiInjection; }
    public boolean switchThinking() { return thinking = !thinking; }
    public boolean switchVoice() { return voice = !voice; }
    public boolean switchEmbedding() { return embedding = !embedding; }
    public boolean switchEmbeddingAuth() { return embeddingAuth = !embeddingAuth; }
    public boolean switchCustom() { return custom = !custom; }
    public boolean switchAutoReply() { return autoReply = !autoReply; }

    public boolean switchImageCollect() { return imageCollect = !imageCollect; }
    public boolean switchMessageCollect() { return messageCollect = !messageCollect; }
    public boolean switchKeywordDetect() { return keywordDetect = !keywordDetect; }
    public boolean switchPokeDetect() { return pokeDetect = !pokeDetect; }
    public boolean switchRecallDetect() { return recallDetect = !recallDetect; }

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
                limitScope,
                limitCapacity,
                limitRefill,
                limitInterval,
                chatScope,
                antiInjection ? "ON" : "OFF",
                thinking ? "ON" : "OFF",
                voice ? "ON" : "OFF",
                embedding ? "ON" : "OFF",
                embeddingAuth ? "ON" : "OFF",
                custom ? "ON" : "OFF",
                autoReply ? "ON" : "OFF",
                replyFrequency,
                imageCollect ? "ON" : "OFF",
                messageCollect ? "ON" : "OFF",
                keywordDetect ? "ON" : "OFF",
                pokeDetect ? "ON" : "OFF",
                recallDetect ? "ON" : "OFF",
                guessCropRatio,
                guessTransparentRatio,
                guessPadding
        );
    }
}
