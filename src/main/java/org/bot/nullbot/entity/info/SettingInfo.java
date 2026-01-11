package org.bot.nullbot.entity.info;

import lombok.Data;
import org.bot.nullbot.config.DefaultConfig;

@Data
public class SettingInfo
{
    private Long groupId;

    private Boolean imageCollect;
    private Boolean keywordDetect;
    private Boolean pokeDetect;
    private Boolean messageCollect;
    private Boolean recallDetect;

    public SettingInfo(Long groupId, DefaultConfig config) {
        this.groupId = groupId;

        this.imageCollect = config.getImageCollect();
        this.keywordDetect = config.getKeywordDetect();
        this.pokeDetect = config.getPokeDetect();
        this.messageCollect = config.getMessageCollect();
        this.recallDetect = config.getRecallDetect();
    }

    public boolean switchImageCollect() { return this.imageCollect = !imageCollect; }
    public boolean switchKeywordDetect() { return this.keywordDetect = !keywordDetect; }
    public boolean switchPokeDetect() { return this.pokeDetect = !pokeDetect; }
    public boolean switchMessageCollect() { return this.messageCollect = !messageCollect; }
    public boolean switchRecallDetect() { return this.recallDetect = !recallDetect; }

    @Override
    public String toString() {
        return String.format("""
                imageCollect -> %s
                keywordDetect -> %s
                pokeDetect -> %s
                messageCollect -> %s
                recallDetect -> %s""",

                imageCollect ? "ON" : "OFF",
                keywordDetect ? "ON" : "OFF",
                pokeDetect ? "ON" : "OFF",
                messageCollect ? "ON" : "OFF",
                recallDetect ? "ON" : "OFF"
        );
    }
}
