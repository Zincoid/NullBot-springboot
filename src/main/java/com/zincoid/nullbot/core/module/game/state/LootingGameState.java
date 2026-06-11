package com.zincoid.nullbot.core.module.game.state;

import com.zincoid.nullbot.core.module.game.model.AiEnemy;
import com.zincoid.nullbot.core.module.game.model.LootingMap;
import com.zincoid.nullbot.core.module.game.model.LootingPlayer;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
