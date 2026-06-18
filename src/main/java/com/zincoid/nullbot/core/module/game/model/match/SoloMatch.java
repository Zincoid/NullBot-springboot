package com.zincoid.nullbot.core.module.game.model.match;

import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import lombok.Getter;

import java.util.List;

@Getter
public class SoloMatch extends Match {

    private final Player player;

    public SoloMatch(String id, String type, Player player) {
        super(id, type);
        this.player = player;
    }

    @Override
    public List<Player> getPlayers() {
        return List.of(player);
    }
}
