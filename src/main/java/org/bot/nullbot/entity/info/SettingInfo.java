package org.bot.nullbot.entity.info;

import lombok.Data;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.config.DefaultConfig;
import org.bot.nullbot.enums.Scope;

@Data
public class SettingInfo
{
    private Long groupId;

    private Scope scope;
    private boolean antiInjection;
    private boolean thinking;
    private boolean embedding;
    private boolean embeddingAuth;

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

        this.imageCollect = config.getImageCollect();
        this.messageCollect = config.getMessageCollect();
        this.keywordDetect = config.getKeywordDetect();
        this.pokeDetect = config.getPokeDetect();
        this.recallDetect = config.getRecallDetect();

        this.guessRatio = config.getGuessRatio();
        this.guessPadding = config.getGuessPadding();
    }

    public Scope switchScope() { return scope = scope.next(); }
    public boolean switchAntiInjection() { return antiInjection = !antiInjection; }
    public boolean switchThinking() { return thinking = !thinking; }
    public boolean switchEmbedding() { return embedding = !embeddingAuth; }
    public boolean switchEmbeddingAuth() { return embeddingAuth = !embeddingAuth; }

    public boolean switchImageCollect() { return imageCollect = !imageCollect; }
    public boolean switchMessageCollect() { return messageCollect = !messageCollect; }
    public boolean switchKeywordDetect() { return keywordDetect = !keywordDetect; }
    public boolean switchPokeDetect() { return pokeDetect = !pokeDetect; }
    public boolean switchRecallDetect() { return recallDetect = !recallDetect; }

    @Override
    public String toString() {
        return String.format("""
                 ◉ Monitor 设置
                ├ 图片收集 - %s
                ├ 消息收集 - %s
                ├ 关键词检测 - %s
                ├ 戳一戳检测 - %s
                └ 防撤回 - %s
                 ◉ Guess 设置
                ├ 切割比例 - %s
                └ 内边距 - %s""",

                imageCollect ? "ON" : "OFF",
                messageCollect ? "ON" : "OFF",
                keywordDetect ? "ON" : "OFF",
                pokeDetect ? "ON" : "OFF",
                recallDetect ? "ON" : "OFF",
                guessRatio, guessPadding
        );
    }
}
