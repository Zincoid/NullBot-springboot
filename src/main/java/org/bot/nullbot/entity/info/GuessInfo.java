package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GuessInfo {
    private String name;
    private String path;
    private int times;
}
