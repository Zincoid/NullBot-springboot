package com.zincoid.nullbot.core.model.game.basic.state.impl;

import com.zincoid.nullbot.core.model.game.looting.AiEnemy;
import com.zincoid.nullbot.core.model.game.looting.LootingMap;
import com.zincoid.nullbot.core.model.game.looting.LootingPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.zincoid.nullbot.core.model.game.basic.state.GameState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class LootingGameState extends GameState {

    private LootingMap map;

    private Map<Long, LootingPlayer> players = new HashMap<>();
    private List<AiEnemy> enemies = new ArrayList<>();

    private int tick = 0;
    private boolean finished = false;
}
