package com.zincoid.nullbot.core.module.game.impl.looting;

import com.zincoid.nullbot.core.module.game.impl.looting.model.AiEnemy;
import com.zincoid.nullbot.core.module.game.impl.looting.model.LootingMap;
import com.zincoid.nullbot.core.module.game.impl.looting.model.LootingPlayer;
import com.zincoid.nullbot.core.module.game.framework.GameState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
public class LootingState extends GameState {

    private LootingMap map;
    private int tick;

    private Map<Long, LootingPlayer> players = new ConcurrentHashMap<>();
    private List<AiEnemy> enemies = new CopyOnWriteArrayList<>();

    private boolean finished;
}
