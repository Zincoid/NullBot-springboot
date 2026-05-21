package com.zincoid.nullbot.core.model.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import com.zincoid.nullbot.core.model.po.FilePO;

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
