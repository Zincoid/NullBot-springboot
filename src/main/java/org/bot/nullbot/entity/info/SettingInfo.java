package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bot.nullbot.config.prop.DefaultProperties;
import org.bot.nullbot.entity.ChatOption;
import org.bot.nullbot.enums.ChatScope;
import org.bot.nullbot.enums.LimitScope;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingInfo {

    private Long groupId;

    private LimitScope limitScope;
    private int limitCapacity;
    private int limitRefill;
    private int limitInterval;

    private ChatScope chatScope;
    private boolean antiInjection;
    private boolean thinking;
    private boolean voice;
    private boolean embedding;
    private boolean embeddingAuth;
    private boolean custom;

    private boolean autoReply;
    private double replyFrequency;

    private boolean imageCollect;
    private boolean messageCollect;
    private boolean keywordDetect;
    private boolean pokeDetect;
    private boolean recallDetect;

    private double guessCropRatio;
    private double guessTransparentRatio;
    private int guessPadding;

    public SettingInfo(Long groupId, DefaultProperties defaultProperties) {
        this.groupId = groupId;

        this.limitScope = defaultProperties.getLimitScope();
        this.limitCapacity = defaultProperties.getLimitCapacity();
        this.limitRefill = defaultProperties.getLimitRefill();
        this.limitInterval = defaultProperties.getLimitInterval();

        this.chatScope = defaultProperties.getChatScope();
        this.antiInjection = defaultProperties.isAntiInjection();
        this.thinking = defaultProperties.isThinking();
        this.voice = defaultProperties.isVoice();
        this.embedding = defaultProperties.isEmbedding();
        this.embeddingAuth = defaultProperties.isEmbeddingAuth();
        this.custom = defaultProperties.isCustom();

        this.autoReply = defaultProperties.isAutoReply();
        this.replyFrequency = defaultProperties.getReplyFrequency();

        this.imageCollect = defaultProperties.isImageCollect();
        this.messageCollect = defaultProperties.isMessageCollect();
        this.keywordDetect = defaultProperties.isKeywordDetect();
        this.pokeDetect = defaultProperties.isPokeDetect();
        this.recallDetect = defaultProperties.isRecallDetect();

        this.guessCropRatio = defaultProperties.getGuessCropRatio();
        this.guessTransparentRatio = defaultProperties.getGuessTransparentRatio();
        this.guessPadding = defaultProperties.getGuessPadding();
    }

    public LimitScope switchLimitScope() { return limitScope = limitScope.next(); }

    public ChatOption getChatOption() {
        return new ChatOption(chatScope, antiInjection, thinking, voice, embedding, embeddingAuth, custom);
    }

    public ChatScope switchChatScope() { return chatScope = chatScope.next(); }
    public boolean switchAntiInjection() { return antiInjection = !antiInjection; }
    public boolean switchThinking() { return thinking = !thinking; }
    public boolean switchVoice() { return voice = !voice; }
    public boolean switchEmbedding() { return embedding = !embeddingAuth; }
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
