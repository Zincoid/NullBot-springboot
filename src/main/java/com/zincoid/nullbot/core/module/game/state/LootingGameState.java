package com.zincoid.nullbot.core.module.game.state;

import com.zincoid.nullbot.core.module.game.model.AiEnemy;
import com.zincoid.nullbot.core.module.game.model.LootingMap;
import com.zincoid.nullbot.core.module.game.model.LootingPlayer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
public class LootingGameState extends GameState {

    private LootingMap map;
    private int tick = 0;

    private Map<Long, LootingPlayer> players = new ConcurrentHashMap<>();
    private List<AiEnemy> enemies = new CopyOnWriteArrayList<>();

    @Getter(AccessLevel.NONE)
    private boolean finished = false;

    @Override
    public boolean isFinished() {
        return finished;
    }
}
