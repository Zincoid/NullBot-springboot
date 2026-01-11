package org.bot.nullbot.entity.info;

import lombok.Data;
import org.bot.nullbot.config.DefaultConfig;

@Data
public class SettingInfo
{
    private Long groupId;

    private Boolean imageCollect;
    private Boolean messageCollect;
    private Boolean keywordDetect;
    private Boolean pokeDetect;
    private Boolean recallDetect;

    private Double guessRatio;
    private Integer guessPadding;

    public SettingInfo(Long groupId, DefaultConfig config) {
        this.groupId = groupId;

        this.imageCollect = config.getImageCollect();
        this.messageCollect = config.getMessageCollect();
        this.keywordDetect = config.getKeywordDetect();
        this.pokeDetect = config.getPokeDetect();
        this.recallDetect = config.getRecallDetect();

        this.guessRatio = config.getGuessRatio();
        this.guessPadding = config.getGuessPadding();
    }

    public boolean switchImageCollect() { return this.imageCollect = !imageCollect; }
    public boolean switchMessageCollect() { return this.messageCollect = !messageCollect; }
    public boolean switchKeywordDetect() { return this.keywordDetect = !keywordDetect; }
    public boolean switchPokeDetect() { return this.pokeDetect = !pokeDetect; }
    public boolean switchRecallDetect() { return this.recallDetect = !recallDetect; }

    @Override
    public String toString() {
        return String.format("""
                [Monitor 设置]
                图片收集 - %s
                消息收集 - %s
                关键词检测 - %s
                戳一戳检测 - %s
                防撤回 - %s
                [Guess 设置]
                切割比例 - %s
                内边距 - %s
                """,

                imageCollect ? "ON" : "OFF",
                messageCollect ? "ON" : "OFF",
                keywordDetect ? "ON" : "OFF",
                pokeDetect ? "ON" : "OFF",
                recallDetect ? "ON" : "OFF",
                guessPadding, guessRatio
        );
    }
}
