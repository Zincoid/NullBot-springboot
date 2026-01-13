package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bot.nullbot.config.DefaultConfig;
import org.bot.nullbot.enums.Scope;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingInfo
{
    private Long groupId;

    private Scope scope;
    private boolean antiInjection;
    private boolean thinking;
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

    private double guessRatio;
    private int guessPadding;

    public SettingInfo(Long groupId, DefaultConfig config) {
        this.groupId = groupId;

        this.scope = config.getScope();
        this.antiInjection = config.isAntiInjection();
        this.thinking = config.isThinking();
        this.embedding = config.isEmbedding();
        this.embeddingAuth = config.isEmbeddingAuth();
        this.custom = config.isCustom();

        this.autoReply = config.isAutoReply();
        this.replyFrequency = config.getReplyFrequency();

        this.imageCollect = config.isImageCollect();
        this.messageCollect = config.isMessageCollect();
        this.keywordDetect = config.isKeywordDetect();
        this.pokeDetect = config.isPokeDetect();
        this.recallDetect = config.isRecallDetect();

        this.guessRatio = config.getGuessRatio();
        this.guessPadding = config.getGuessPadding();
    }

    public Scope switchScope() { return scope = scope.next(); }
    public boolean switchAntiInjection() { return antiInjection = !antiInjection; }
    public boolean switchThinking() { return thinking = !thinking; }
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
                 ◉ AI 设置
                ├ 会话范围 - %s
                ├ 防注模式 - %s
                ├ 思考模式 - %s
                ├ 指令模式 - %s
                ├ 指令校验 - %s
                └ 自定模式 - %s
                
                └ 回复频率 - %s
                 ◉ Monitor 设置
                ├ 图片收集 - %s
                ├ 消息收集 - %s
                ├ 词语检测 - %s
                ├ 戳戳检测 - %s
                └ 撤回检测 - %s
                 ◉ Guess 设置
                ├ 切割比例 - %s
                └ 切割边距 - %s""",
                scope,
                antiInjection ? "ON" : "OFF",
                thinking ? "ON" : "OFF",
                embedding ? "ON" : "OFF",
                embeddingAuth ? "ON" : "OFF",
                custom ? "ON" : "OFF",
                imageCollect ? "ON" : "OFF",
                messageCollect ? "ON" : "OFF",
                keywordDetect ? "ON" : "OFF",
                pokeDetect ? "ON" : "OFF",
                recallDetect ? "ON" : "OFF",
                guessRatio, guessPadding
        );
    }
}
