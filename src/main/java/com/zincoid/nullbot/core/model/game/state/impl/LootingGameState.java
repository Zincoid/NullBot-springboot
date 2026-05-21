package com.zincoid.nullbot.core.model.game.state.impl;

import com.zincoid.nullbot.core.model.game.looting.AiEnemyState;
import com.zincoid.nullbot.core.model.game.looting.LootingMap;
import com.zincoid.nullbot.core.model.game.looting.LootingPlayerState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.zincoid.nullbot.core.model.game.state.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class LootingGameState extends GameState {

    private LootingMap map;

    private Map<Long, LootingPlayerState> players = new HashMap<>();
    private List<AiEnemyState> enemies = new ArrayList<>();

    private int tick = 0;
    private boolean finished = false;
}
