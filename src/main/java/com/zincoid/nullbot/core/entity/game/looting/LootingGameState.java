package com.zincoid.nullbot.core.entity.game.looting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.zincoid.nullbot.core.entity.game.GameState;

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
