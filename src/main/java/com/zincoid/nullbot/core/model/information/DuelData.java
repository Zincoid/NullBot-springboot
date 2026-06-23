package com.zincoid.nullbot.core.model.information;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class DuelData {

    private final Map<Integer, Integer> left;
    private final Map<Integer, Integer> right;
    private final String winner;
}
