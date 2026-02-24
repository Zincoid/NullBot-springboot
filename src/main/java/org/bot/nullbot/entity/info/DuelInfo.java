package org.bot.nullbot.entity.info;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class DuelInfo
{
    private final Map<Integer, Integer> left;
    private final Map<Integer, Integer> right;
    private final String winner;

    @Override
    public String toString() {
        return "DuelInfo{" +
                "left=" + left +
                ", right=" + right +
                ", winner='" + winner + '\'' +
                '}';
    }
}
