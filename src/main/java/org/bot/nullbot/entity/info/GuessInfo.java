package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bot.nullbot.entity.po.FilePO;

@Data
@AllArgsConstructor
public class GuessInfo {

    private String name;
    private FilePO character;
    private int times;

    public String getPath() {
        return character.getPath();
    }
}
