package org.bot.nullbot.entity.setting;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MonitorOption {

    private final Long groupId;

    private boolean imageCollect = false;
    private boolean messageCollect = true;
    private boolean keywordDetect = true;
    private boolean pokeDetect = true;
    private boolean recallDetect = false;

    public boolean switchImageCollect() {
        return imageCollect = !imageCollect;
    }

    public boolean switchMessageCollect() {
        return messageCollect = !messageCollect;
    }

    public boolean switchKeywordDetect() {
        return keywordDetect = !keywordDetect;
    }

    public boolean switchPokeDetect() {
        return pokeDetect = !pokeDetect;
    }

    public boolean switchRecallDetect() {
        return recallDetect = !recallDetect;
    }
}
